package api.recoil.event.bus;

/**
 * Base class for all events in the Recoil event system.
 * <p>
 * All events must extend this class. Events are dispatched through the
 * {@link EventBus} and delivered to registered listeners in priority order.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * public class PlayerJoinEvent extends Event {
 *     private final String playerName;
 *
 *     public PlayerJoinEvent(String playerName) {
 *         this.playerName = playerName;
 *     }
 *
 *     public String getPlayerName() {
 *         return playerName;
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @param <T> The type of the event (for fluent API pattern)
 * @see EventBus
 * @see CancellableEvent
 * @since 1.0.0
 */
public abstract class Event<T extends Event<T>> {

	/**
	 * The timestamp when this event was created, in nanoseconds.
	 * <p>
	 * Uses {@link System#nanoTime()} for high-resolution timing.
	 * </p>
	 */
	private final long creationTimeNanos;

	/**
	 * The thread that created this event.
	 * <p>
	 * Useful for debugging and ensuring thread-safety constraints.
	 * </p>
	 */
	private final Thread creationThread;

	/**
	 * Constructs a new event with the current timestamp and thread.
	 */
	protected Event() {
		this.creationTimeNanos = System.nanoTime();
		this.creationThread = Thread.currentThread();
	}

	/**
	 * Returns the creation timestamp of this event in nanoseconds.
	 *
	 * @return the creation time in nanoseconds
	 */
	public final long getCreationTimeNanos() {
		return creationTimeNanos;
	}

	/**
	 * Returns the thread that created this event.
	 *
	 * @return the creation thread
	 */
	public final Thread getCreationThread() {
		return creationThread;
	}

	/**
	 * Returns this event cast to its concrete type.
	 * <p>
	 * Used for fluent API patterns in subclasses.
	 * </p>
	 *
	 * @return this event cast to type T
	 */
	@SuppressWarnings("unchecked")
	public final T self() {
		return (T) this;
	}

	/**
	 * Returns the simple class name of this event for logging purposes.
	 *
	 * @return the event's simple class name
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
