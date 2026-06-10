package api.recoil.event.bus;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * A weak-reference wrapper for event listeners.
 * <p>
 * Use this wrapper when registering listeners on objects that may be garbage
 * collected before they can be explicitly unregistered. The EventBus will
 * automatically clean up listeners whose referent has been collected.
 * </p>
 * <p>
 * This is particularly useful for:
 * </p>
 * <ul>
 *   <li>Temporary listeners that should auto-unregister when the owner is GC'd</li>
 *   <li>Listeners registered on objects with unknown or complex lifecycles</li>
 *   <li>Preventing memory leaks in long-running event subscriptions</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * // The listener will be automatically removed when 'handler' is garbage collected
 * MyHandler handler = new MyHandler();
 * EventBus.subscribe(PlayerTickEvent.class)
 *     .weak(handler::onTick)
 *     .listen(event -> { ... });
 * }</pre>
 * </p>
 *
 * @param <E> the event type
 * @see EventBus
 * @since 1.0.0
 */
public final class WeakEventListener<E extends Event<E>> implements EventListener<E> {

	/**
	 * Weak reference to the delegate listener.
	 */
	private final WeakReference<EventListener<E>> listenerReference;

	/**
	 * The class of the delegate listener for logging/debugging.
	 */
	private final String listenerClassName;

	/**
	 * Creates a new weak event listener wrapper.
	 *
	 * @param listener the delegate listener to wrap (must not be null)
	 * @throws NullPointerException if listener is null
	 */
	public WeakEventListener(EventListener<E> listener) {
		Objects.requireNonNull(listener, "Listener cannot be null");
		this.listenerReference = new WeakReference<>(listener);
		this.listenerClassName = listener.getClass().getName();
	}

	/**
	 * Returns whether the underlying referent has been garbage collected.
	 * <p>
	 * The EventBus uses this to clean up dead listeners during event posting.
	 * </p>
	 *
	 * @return true if the referent has been collected, false otherwise
	 */
	public boolean isExpired() {
		return listenerReference.get() == null;
	}

	/**
	 * Returns the delegate listener, or null if it has been garbage collected.
	 *
	 * @return the delegate listener, or null if expired
	 */
	public EventListener<E> getDelegate() {
		return listenerReference.get();
	}

	/**
	 * Called when an event is posted.
	 * <p>
	 * If the delegate listener has been garbage collected, this method
	 * does nothing and returns silently. The EventBus will clean up
	 * expired listeners during subsequent operations.
	 * </p>
	 *
	 * @param event the event being posted
	 */
	@Override
	public void onEvent(E event) {
		EventListener<E> delegate = listenerReference.get();
		if (delegate != null) {
			delegate.onEvent(event);
		}
	}

	/**
	 * Returns the class name of the delegate listener for logging.
	 *
	 * @return the delegate listener's class name
	 */
	public String getListenerClassName() {
		return listenerClassName;
	}

	@Override
	public String toString() {
		EventListener<E> delegate = listenerReference.get();
		return "WeakEventListener[" + (delegate != null ? delegate : "EXPIRED:" + listenerClassName) + "]";
	}
}
