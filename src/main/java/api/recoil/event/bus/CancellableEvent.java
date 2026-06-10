package api.recoil.event.bus;

/**
 * Base class for events that can be cancelled.
 * <p>
 * Once an event is cancelled, it remains cancelled for the remainder of
 * dispatch. Cancellation is one-way — there is no "un-cancel" operation.
 * </p>
 * <p>
 * Listeners at priorities higher than {@link EventPriority#MONITOR} can cancel
 * events. MONITOR listeners receive events regardless of cancellation state
 * but cannot cancel them.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * public class EntityHurtEvent extends CancellableEvent<EntityHurtEvent> {
 *     private final Entity entity;
 *     private float damage;
 *
 *     public EntityHurtEvent(Entity entity, float damage) {
 *         this.entity = entity;
 *         this.damage = damage;
 *     }
 *
 *     public Entity getEntity() { return entity; }
 *     public float getDamage() { return damage; }
 *     public void setDamage(float damage) { this.damage = damage; }
 * }
 *
 * // Usage:
 * if (event instanceof CancellableEvent<?> cancelled) {
 *     cancelled.cancel(); // Prevent the damage
 * }
 * }</pre>
 * </p>
 *
 * @param <T> The type of the event (for fluent API pattern)
 * @see Event
 * @see EventBus#postCancellable(CancellableEvent)
 * @since 1.0.0
 */
public abstract class CancellableEvent<T extends CancellableEvent<T>> extends Event<T> {

	/**
	 * Flag indicating whether this event has been cancelled.
	 * <p>
	 * Uses volatile for thread-safe reads without requiring synchronization.
	 * </p>
	 */
	private volatile boolean cancelled = false;

	/**
	 * Constructs a new cancellable event.
	 */
	protected CancellableEvent() {
		super();
	}

	/**
	 * Returns whether this event has been cancelled.
	 * <p>
	 * Thread-safe — can be called from any thread.
	 * </p>
	 *
	 * @return true if the event is cancelled, false otherwise
	 */
	public final boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Cancels this event.
	 * <p>
	 * Once cancelled, an event cannot be un-cancelled. This operation is
	 * thread-safe and idempotent — calling it multiple times has no
	 * additional effect.
	 * </p>
	 * <p>
	 * Note: Listeners at {@link EventPriority#MONITOR} priority should
	 * not call this method as it will have no effect during their execution.
	 * </p>
	 *
	 * @return this event for fluent API usage
	 */
	public final T cancel() {
		this.cancelled = true;
		return self();
	}

	/**
	 * Returns a string representation including the cancellation state.
	 *
	 * @return string representation with cancellation status
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + (cancelled ? " [CANCELLED]" : "");
	}
}
