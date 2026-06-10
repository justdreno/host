package api.recoil.event.bus;

import java.util.Objects;

/**
 * Exception thrown when an event listener encounters an error.
 * <p>
 * This exception wraps the original throwable and includes contextual
 * information about the event, listener, priority, and phase for debugging.
 * </p>
 * <p>
 * EventBus catches all listener exceptions to ensure one listener failure
 * does not prevent other listeners from receiving the event. Exceptions
 * are wrapped in EventException and logged with full context.
 * </p>
 *
 * @since 1.0.0
 */
public final class EventException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/** The event type that failed to process. */
	private final Class<? extends Event<?>> eventType;

	/** The listener's class name. */
	private final String listenerClassName;

	/** The method name (if using @EventHandler reflection). */
	private final String methodName;

	/** The priority at which the listener was registered. */
	private final EventPriority priority;

	/** The phase for fine-grained ordering. */
	private final EventPhase phase;

	/**
	 * Creates a new event exception with full context.
	 *
	 * @param eventType the event type being processed
	 * @param listener the listener that threw the exception
	 * @param methodName the method name (may be null for lambdas)
	 * @param priority the listener's priority
	 * @param phase the listener's phase
	 * @param cause the original exception
	 */
	public EventException(
			Class<? extends Event<?>> eventType,
			EventListener<?> listener,
			String methodName,
			EventPriority priority,
			EventPhase phase,
			Throwable cause
	) {
		super(buildMessage(eventType, listener, methodName, priority, phase, cause), cause);
		this.eventType = eventType;
		this.listenerClassName = listener != null ? listener.getClass().getName() : "unknown";
		this.methodName = methodName;
		this.priority = priority;
		this.phase = phase;
	}

	/**
	 * Returns the event type that failed to process.
	 *
	 * @return the event class
	 */
	public Class<? extends Event<?>> getEventType() {
		return eventType;
	}

	/**
	 * Returns the listener's class name.
	 *
	 * @return the listener class name
	 */
	public String getListenerClassName() {
		return listenerClassName;
	}

	/**
	 * Returns the method name if applicable, or null.
	 *
	 * @return the method name, or null for lambdas
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Returns the priority at which the listener was registered.
	 *
	 * @return the event priority
	 */
	public EventPriority getPriority() {
		return priority;
	}

	/**
	 * Returns the phase for fine-grained ordering.
	 *
	 * @return the event phase
	 */
	public EventPhase getPhase() {
		return phase;
	}

	/**
	 * Builds a detailed error message for logging.
	 */
	private static String buildMessage(
			Class<? extends Event<?>> eventType,
			EventListener<?> listener,
			String methodName,
			EventPriority priority,
			EventPhase phase,
			Throwable cause
	) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("Exception in event listener: ");
		sb.append("event=").append(eventType != null ? eventType.getSimpleName() : "unknown");
		sb.append(", listener=").append(listener != null ? listener.getClass().getName() : "unknown");

		if (methodName != null) {
			sb.append("::").append(methodName);
		}

		sb.append(", priority=").append(priority);
		sb.append(", phase=").append(phase);

		if (cause != null) {
			sb.append(", cause=").append(cause.getClass().getSimpleName());
			sb.append(": ").append(cause.getMessage());
		}

		return sb.toString();
	}

	/**
	 * Creates a builder for constructing an EventException.
	 *
	 * @param eventType the event type
	 * @return a new builder
	 */
	public static Builder builder(Class<? extends Event<?>> eventType) {
		return new Builder(eventType);
	}

	/**
	 * Builder for EventException instances.
	 */
	public static final class Builder {
		private final Class<? extends Event<?>> eventType;
		private EventListener<?> listener;
		private String methodName;
		private EventPriority priority = EventPriority.NORMAL;
		private EventPhase phase = EventPhase.DEFAULT;
		private Throwable cause;

		private Builder(Class<? extends Event<?>> eventType) {
			this.eventType = Objects.requireNonNull(eventType, "Event type cannot be null");
		}

		/**
		 * Sets the listener that threw the exception.
		 *
		 * @param listener the listener
		 * @return this builder
		 */
		public Builder listener(EventListener<?> listener) {
			this.listener = listener;
			return this;
		}

		/**
		 * Sets the method name (for reflected event handlers).
		 *
		 * @param methodName the method name
		 * @return this builder
		 */
		public Builder methodName(String methodName) {
			this.methodName = methodName;
			return this;
		}

		/**
		 * Sets the listener's priority.
		 *
		 * @param priority the priority
		 * @return this builder
		 */
		public Builder priority(EventPriority priority) {
			this.priority = priority != null ? priority : EventPriority.NORMAL;
			return this;
		}

		/**
		 * Sets the listener's phase.
		 *
		 * @param phase the phase
		 * @return this builder
		 */
		public Builder phase(EventPhase phase) {
			this.phase = phase != null ? phase : EventPhase.DEFAULT;
			return this;
		}

		/**
		 * Sets the cause of the exception.
		 *
		 * @param cause the cause
		 * @return this builder
		 */
		public Builder cause(Throwable cause) {
			this.cause = cause;
			return this;
		}

		/**
		 * Builds the EventException.
		 *
		 * @return a new EventException
		 */
		public EventException build() {
			return new EventException(eventType, listener, methodName, priority, phase, cause);
		}
	}
}
