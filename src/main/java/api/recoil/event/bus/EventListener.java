package api.recoil.event.bus;

/**
 * Functional interface for event listeners.
 * <p>
 * Implementations receive events of a specific type and can optionally
 * cancel cancellable events. Listeners are registered with the {@link EventBus}
 * using a builder pattern for configuration.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Lambda syntax
 * EventBus.subscribe(PlayerJoinEvent.class)
 *     .listen(event -> {
 *         System.out.println(event.getPlayerName() + " joined!");
 *     });
 *
 * // Method reference
 * EventBus.subscribe(PlayerTickEvent.class)
 *     .priority(EventPriority.HIGH)
 *     .listen(this::onPlayerTick);
 *
 * // With phase
 * EventBus.subscribe(EntityHurtEvent.class)
 *     .priority(EventPriority.NORMAL)
 *     .phase(EventPhase.EARLY)
 *     .listen(event -> {
 *         event.setDamage(event.getDamage() * 0.5f);
 *     });
 * }</pre>
 * </p>
 *
 * @param <E> the event type this listener handles
 * @see EventBus
 * @since 1.0.0
 */
@FunctionalInterface
public interface EventListener<E extends Event<E>> {

	/**
	 * Called when an event of type E is posted to the event bus.
	 * <p>
	 * Implementations should:
	 * </p>
	 * <ul>
	 *   <li>Handle the event as quickly as possible</li>
	 *   <li>Not throw exceptions (exceptions are caught and logged by EventBus)</li>
	 *   <li>Check {@link CancellableEvent#isCancelled()} for cancellable events</li>
	 * </ul>
	 *
	 * @param event the event being posted
	 */
	void onEvent(E event);
}
