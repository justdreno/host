package api.recoil.event.bus;

import api.recoil.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Central event bus for the Recoil event system.
 * <p>
 * A fully custom, annotation-free, type-safe event bus that supports:
 * </p>
 * <ul>
 *   <li>Priority-based listener ordering (HIGHEST to MONITOR)</li>
 *   <li>Sub-phase ordering for fine-grained control</li>
 *   <li>Cancellable events with one-way cancellation</li>
 *   <li>Weak-reference listeners for automatic cleanup</li>
 *   <li>Async event posting via dedicated executor</li>
 *   <li>Declarative @EventHandler annotation support</li>
 *   <li>Exception isolation between listeners</li>
 * </ul>
 * <p>
 * Thread-safety: All subscriber operations are thread-safe via CopyOnWriteArrayList.
 * Event posting is lock-free and safe to call from any thread.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Lambda subscription
 * EventBus.subscribe(PlayerTickEvent.class)
 *     .priority(EventPriority.HIGH)
 *     .priority(EventPhase.EARLY)
 *     .listen(event -> { ... });
 *
 * // Post an event
 * EventBus.post(new PlayerJoinEvent("Steve"));
 *
 * // Post a cancellable event
 * boolean cancelled = EventBus.postCancellable(new EntityHurtEvent(entity, 10.0f));
 * if (cancelled) { // Event was cancelled by a listener }
 *
 * // Async posting
 * EventBus.postAsync(new LogEvent("message"));
 *
 * // Declarative style
 * public class MyHandler {
 *     @EventHandler(priority = EventPriority.HIGH)
 *     public void onTick(PlayerTickEvent event) { ... }
 * }
 * EventBus.register(new MyHandler());
 * }</pre>
 * </p>
 *
 * @since 1.0.0
 */
public final class EventBus {

	/** Singleton instance. */
	private static final EventBus INSTANCE = new EventBus();

	/** Logger for event system messages. */
	private static final Logger LOGGER = LogManager.getLogger(ModInfo.MOD_ID + "/EventBus");

	/** Executor for async event posting. */
	private final ScheduledExecutorService asyncExecutor;

	/** Map of event type to sorted subscriber lists. */
	private final ConcurrentMap<Class<?>, CopyOnWriteArrayList<Subscriber<?>>> subscribers;

	/** Method handle lookup for reflection. */
	private final MethodHandles.Lookup methodLookup;

	/** Counter for generating unique subscriber IDs. */
	private final AtomicInteger subscriberIdCounter;

	/**
	 * Returns the singleton EventBus instance.
	 *
	 * @return the EventBus instance
	 */
	public static EventBus getInstance() {
		return INSTANCE;
	}

	/**
	 * Private constructor for singleton pattern.
	 */
	private EventBus() {
		this.subscribers = new ConcurrentHashMap<>();
		this.methodLookup = MethodHandles.lookup();
		this.subscriberIdCounter = new AtomicInteger(0);
		this.asyncExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "Recoil-EventBus-Async");
			t.setDaemon(true);
			t.setPriority(Thread.NORM_PRIORITY - 1);
			return t;
		});
		LOGGER.info("EventBus initialized");
	}

	// ==================== Subscription Builder ====================

	/**
	 * Creates a subscription builder for the given event type.
	 * <p>
	 * The builder pattern allows fluent configuration of priority, phase,
	 * and other listener options before finalizing with {@code listen()}.
	 * </p>
	 *
	 * @param <E> the event type
	 * @param eventType the event class
	 * @return a new subscription builder
	 * @throws NullPointerException if eventType is null
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Event<E>> SubscriptionBuilder<E> subscribe(Class<E> eventType) {
		Objects.requireNonNull(eventType, "Event type cannot be null");
		return new SubscriptionBuilder<>(INSTANCE, eventType);
	}

	// ==================== Post Methods ====================

	/**
	 * Posts an event to all registered listeners.
	 * <p>
	 * Listeners are invoked in priority order (HIGHEST → LOWEST → MONITOR).
	 * Exceptions in listeners are caught, logged, and do not prevent other
	 * listeners from receiving the event.
	 * </p>
	 *
	 * @param <E> the event type
	 * @param event the event to post
	 * @throws NullPointerException if event is null
	 */
	@SuppressWarnings("unchecked")
	public <E extends Event<E>> void post(E event) {
		Objects.requireNonNull(event, "Event cannot be null");

		Class<E> eventType = (Class<E>) event.getClass();
		CopyOnWriteArrayList<Subscriber<?>> list = subscribers.get(eventType);

		if (list == null || list.isEmpty()) {
			return;
		}

		boolean isCancellable = event instanceof CancellableEvent;

		for (Subscriber<?> subscriber : list) {
			try {
				@SuppressWarnings("rawtypes")
				Subscriber<E> sub = (Subscriber<E>) subscriber;

				// For cancellable events, check if cancelled (except MONITOR)
				if (isCancellable && subscriber.priority != EventPriority.MONITOR) {
					if (((CancellableEvent<?>) event).isCancelled()) {
						continue; // Skip non-MONITOR listeners if cancelled
					}
				}

				sub.invoke(event);
			} catch (Exception e) {
				handleException(event, subscriber, null, e);
			}
		}
	}

	/**
	 * Posts a cancellable event and returns whether it was cancelled.
	 * <p>
	 * Listeners are invoked in priority order. If a listener cancels the event,
	 * subsequent listeners (except MONITOR) will not receive the event.
	 * </p>
	 *
	 * @param <E> the event type
	 * @param event the cancellable event to post
	 * @return true if the event was cancelled, false otherwise
	 * @throws NullPointerException if event is null
	 */
	public <E extends CancellableEvent<E>> boolean postCancellable(E event) {
		Objects.requireNonNull(event, "Event cannot be null");
		post(event);
		return event.isCancelled();
	}

	/**
	 * Posts an event asynchronously to all registered listeners.
	 * <p>
	 * The event is posted on a dedicated daemon thread. Any exceptions
	 * are caught and logged. This is a fire-and-forget operation.
	 * </p>
	 * <p>
	 * Async posting is useful for:
	 * </p>
	 * <ul>
	 *   <li>Logging events</li>
	 *   <li>Metrics collection</li>
	 *   <li>Non-critical notifications</li>
	 * </ul>
	 *
	 * @param <E> the event type
	 * @param event the event to post
	 * @throws NullPointerException if event is null
	 */
	public <E extends Event<E>> void postAsync(E event) {
		Objects.requireNonNull(event, "Event cannot be null");
		asyncExecutor.submit(() -> {
			try {
				post(event);
			} catch (Exception e) {
				LOGGER.error("Error in async event posting for {}: {}",
						event.getClass().getSimpleName(), e.getMessage(), e);
			}
		});
	}

	/**
	 * Posts an event asynchronously with a delay.
	 *
	 * @param <E> the event type
	 * @param event the event to post
	 * @param delay the delay before posting
	 * @param unit the time unit
	 * @throws NullPointerException if event or unit is null
	 */
	public <E extends Event<E>> void postAsyncDelayed(E event, long delay, TimeUnit unit) {
		Objects.requireNonNull(event, "Event cannot be null");
		Objects.requireNonNull(unit, "Time unit cannot be null");
		asyncExecutor.schedule(() -> post(event), delay, unit);
	}

	// ==================== Declarative Registration ====================

	/**
	 * Registers all {@link EventHandler} annotated methods on the given object.
	 * <p>
	 * Scans the object for methods annotated with @EventHandler and registers
	 * them as subscribers. Methods must have exactly one parameter extending
	 * Event and may be private.
	 * </p>
	 *
	 * @param object the object to scan for event handlers
	 * @throws NullPointerException if object is null
	 */
	public void register(Object object) {
		Objects.requireNonNull(object, "Object cannot be null");

		Class<?> clazz = object.getClass();

		for (Method method : clazz.getDeclaredMethods()) {
			EventHandler annotation = method.getAnnotation(EventHandler.class);
			if (annotation == null) continue;

			// Validate method signature
			if (method.getParameterCount() != 1) {
				LOGGER.warn("@EventHandler method {} must have exactly one parameter", method);
				continue;
			}

			Class<?> paramType = method.getParameterTypes()[0];
			if (!Event.class.isAssignableFrom(paramType)) {
				LOGGER.warn("@EventHandler method {} parameter must extend Event", method);
				continue;
			}

			@SuppressWarnings("unchecked")
			Class<? extends Event<?>> eventType = (Class<? extends Event<?>>) paramType;

			try {
				method.setAccessible(true);
				MethodHandle handle = methodLookup.unreflect(method);

				@SuppressWarnings("unchecked")
				EventListener<? extends Event<?>> listener = e -> {
					try {
						handle.invoke(object, e);
					} catch (Throwable t) {
						throw new RuntimeException(t);
					}
				};

				registerMethod(eventType, listener, method.getName(), annotation.priority(), annotation.phase());
			} catch (IllegalAccessException e) {
				LOGGER.error("Failed to create method handle for @EventHandler method {}: {}",
						method.getName(), e.getMessage());
			}
		}
	}

	/**
	 * Registers a reflected method as an event listener.
	 */
	private <E extends Event<E>> void registerMethod(
			Class<E> eventType,
			EventListener<E> listener,
			String methodName,
			EventPriority priority,
			EventPhase phase
	) {
		Subscriber<E> subscriber = new Subscriber<>(
				subscriberIdCounter.incrementAndGet(),
				eventType,
				listener,
				priority,
				phase,
				false,
				methodName
		);
		addSubscriber(eventType, subscriber);
		LOGGER.debug("Registered @EventHandler '{}' for {} at {}:{}",
				methodName, eventType.getSimpleName(), priority, phase);
	}

	/**
	 * Unregisters all event handlers from the given object.
	 * <p>
	 * Removes any subscribers that were registered via {@link #register(Object)}.
	 * </p>
	 *
	 * @param object the object to unregister
	 * @throws NullPointerException if object is null
	 */
	@SuppressWarnings("unchecked")
	public void unregister(Object object) {
		Objects.requireNonNull(object, "Object cannot be null");

		for (CopyOnWriteArrayList<Subscriber<?>> list : subscribers.values()) {
			list.removeIf(sub -> {
				if (sub.hasOwner(object)) {
					LOGGER.debug("Unregistered subscriber '{}' for {}",
							sub.methodName != null ? sub.methodName : "lambda",
							sub.eventType.getSimpleName());
					return true;
				}
				return false;
			});
		}
	}

	// ==================== Subscriber Management ====================

	/**
	 * Adds a subscriber to the internal map.
	 */
	<E extends Event<E>> void addSubscriber(Class<E> eventType, Subscriber<E> subscriber) {
		CopyOnWriteArrayList<Subscriber<?>> list = subscribers.computeIfAbsent(
				eventType,
				k -> new CopyOnWriteArrayList<>()
		);
		list.add(subscriber);
		list.sort(null); // Sort using Subscriber.compareTo
	}

	/**
	 * Removes a specific subscriber by ID.
	 *
	 * @param subscriberId the subscriber ID to remove
	 */
	public void unsubscribe(int subscriberId) {
		for (CopyOnWriteArrayList<Subscriber<?>> list : subscribers.values()) {
			list.removeIf(sub -> sub.id == subscriberId);
		}
	}

	/**
	 * Removes all subscribers for a given event type.
	 *
	 * @param eventType the event type to clear
	 */
	public void clearSubscribers(Class<? extends Event<?>> eventType) {
		subscribers.remove(eventType);
	}

	/**
	 * Clears all subscribers for all event types.
	 * <p>
	 * Use with caution — this removes all registered listeners.
	 * </p>
	 */
	public void clearAllSubscribers() {
		subscribers.clear();
		LOGGER.warn("All event subscribers cleared");
	}

	// ==================== Exception Handling ====================

	/**
	 * Handles an exception from a listener.
	 */
	private void handleException(Event<?> event, Subscriber<?> subscriber, String methodName, Exception e) {
		EventException ex = EventException.builder(event.getClass())
				.listener(subscriber.listener)
				.methodName(methodName != null ? methodName : subscriber.methodName)
				.priority(subscriber.priority)
				.phase(subscriber.phase)
				.cause(e)
				.build();

		LOGGER.error("Event listener exception: {}", ex.getMessage(), ex.getCause());
	}

	// ==================== Shutdown ====================

	/**
	 * Shuts down the async executor.
	 * <p>
	 * Should be called during mod shutdown to cleanly terminate the
	 * async posting thread.
	 * </p>
	 */
	public void shutdown() {
		LOGGER.info("Shutting down EventBus...");
		asyncExecutor.shutdown();
		try {
			if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
				asyncExecutor.shutdownNow();
			}
		} catch (InterruptedException e) {
			asyncExecutor.shutdownNow();
			Thread.currentThread().interrupt();
		}
		clearAllSubscribers();
		LOGGER.info("EventBus shutdown complete");
	}

	// ==================== Inner Classes ====================

	/**
	 * Internal representation of an event subscriber.
	 *
	 * @param <E> the event type
	 */
	static final class Subscriber<E extends Event<E>> implements Comparable<Subscriber<?>> {
		/** Unique subscriber ID. */
		final int id;
		/** The event type. */
		final Class<E> eventType;
		/** The listener function. */
		final EventListener<E> listener;
		/** Priority for ordering. */
		final EventPriority priority;
		/** Phase for fine-grained ordering. */
		final EventPhase phase;
		/** Whether this is a weak reference. */
		final boolean weak;
		/** Method name for @EventHandler (may be null). */
		final String methodName;
		/** Owner object for @EventHandler (may be null). */
		Object owner;

		Subscriber(int id, Class<E> eventType, EventListener<E> listener,
				   EventPriority priority, EventPhase phase, boolean weak, String methodName) {
			this.id = id;
			this.eventType = eventType;
			this.listener = listener;
			this.priority = priority;
			this.phase = phase;
			this.weak = weak;
			this.methodName = methodName;
		}

		void invoke(E event) {
			listener.onEvent(event);
		}

		boolean hasOwner(Object obj) {
			return owner == obj;
		}

		boolean isExpired() {
			return weak && listener instanceof WeakEventListener<E> w && w.isExpired();
		}

		@Override
		public int compareTo(Subscriber<?> other) {
			// Sort by priority (lowest ordinal = highest priority)
			int priorityCompare = Integer.compare(priority.ordinal(), other.priority.ordinal());
			if (priorityCompare != 0) return priorityCompare;

			// Then by phase
			int phaseCompare = Integer.compare(phase.ordinal(), other.phase.ordinal());
			if (phaseCompare != 0) return phaseCompare;

			// Finally by ID for stable ordering
			return Integer.compare(id, other.id);
		}
	}

	// ==================== Subscription Builder ====================

	/**
	 * Builder for configuring and registering an event listener.
	 *
	 * @param <E> the event type
	 */
	public static final class SubscriptionBuilder<E extends Event<E>> {
		private final EventBus bus;
		private final Class<E> eventType;
		private EventPriority priority = EventPriority.NORMAL;
		private EventPhase phase = EventPhase.DEFAULT;
		private Consumer<SubscriptionBuilder<E>> onUnsubscribe;
		private boolean weak = false;

		SubscriptionBuilder(EventBus bus, Class<E> eventType) {
			this.bus = bus;
			this.eventType = eventType;
		}

		/**
		 * Sets the priority for this listener.
		 *
		 * @param priority the priority
		 * @return this builder
		 * @throws NullPointerException if priority is null
		 */
		public SubscriptionBuilder<E> priority(EventPriority priority) {
			this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
			return this;
		}

		/**
		 * Sets the phase for this listener.
		 *
		 * @param phase the phase
		 * @return this builder
		 * @throws NullPointerException if phase is null
		 */
		public SubscriptionBuilder<E> phase(EventPhase phase) {
			this.phase = Objects.requireNonNull(phase, "Phase cannot be null");
			return this;
		}

		/**
		 * Uses a weak reference for the listener.
		 * <p>
		 * The listener will be automatically removed when its referent
		 * is garbage collected.
		 * </p>
		 *
		 * @return this builder
		 */
		public SubscriptionBuilder<E> weak() {
			this.weak = true;
			return this;
		}

		/**
		 * Registers a callback for when this listener is unsubscribed.
		 *
		 * @param callback the callback
		 * @return this builder
		 */
		public SubscriptionBuilder<E> onUnsubscribe(Consumer<SubscriptionBuilder<E>> callback) {
			this.onUnsubscribe = callback;
			return this;
		}

		/**
		 * Registers the listener and returns a handle for unsubscribing.
		 *
		 * @param listener the event listener
		 * @return a handle to unsubscribe the listener
		 * @throws NullPointerException if listener is null
		 */
		public SubscriptionHandle listen(EventListener<E> listener) {
			Objects.requireNonNull(listener, "Listener cannot be null");

			EventListener<E> actualListener = weak ? wrapWeak(listener) : listener;
			Subscriber<E> subscriber = new Subscriber<>(
					bus.subscriberIdCounter.incrementAndGet(),
					eventType,
					actualListener,
					priority,
					phase,
					weak,
					null
			);

			bus.addSubscriber(eventType, subscriber);
			LOGGER.debug("Registered subscriber #{} for {} at {}:{}",
					subscriber.id, eventType.getSimpleName(), priority, phase);

			return new SubscriptionHandle(subscriber.id, bus, onUnsubscribe, this);
		}

		@SuppressWarnings("unchecked")
		private EventListener<E> wrapWeak(EventListener<E> listener) {
			return event -> {
				if (listener instanceof WeakEventListener wl) {
					wl.onEvent(event);
				}
			};
		}
	}

	/**
	 * Handle for unsubscribing a registered listener.
	 */
	public static final class SubscriptionHandle {
		private final int subscriberId;
		private final EventBus bus;
		private final Consumer<SubscriptionBuilder<?>> onUnsubscribe;
		private final SubscriptionBuilder<?> builder;
		private volatile boolean subscribed = true;

		SubscriptionHandle(int subscriberId, EventBus bus,
						   Consumer<SubscriptionBuilder<?>> onUnsubscribe,
						   SubscriptionBuilder<?> builder) {
			this.subscriberId = subscriberId;
			this.bus = bus;
			this.onUnsubscribe = onUnsubscribe;
			this.builder = builder;
		}

		/**
		 * Unsubscribes this listener from the event bus.
		 * <p>
		 * Safe to call multiple times — subsequent calls are no-ops.
		 * </p>
		 */
		public void unsubscribe() {
			if (subscribed) {
				subscribed = false;
				bus.unsubscribe(subscriberId);
				if (onUnsubscribe != null) {
					onUnsubscribe.accept(builder);
				}
			}
		}

		/**
		 * Returns whether this listener is still subscribed.
		 *
		 * @return true if subscribed, false otherwise
		 */
		public boolean isSubscribed() {
			return subscribed;
		}

		/**
		 * Returns the subscriber ID.
		 *
		 * @return the subscriber ID
		 */
		public int getSubscriberId() {
			return subscriberId;
		}
	}
}
