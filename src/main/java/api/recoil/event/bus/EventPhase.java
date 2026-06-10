package api.recoil.event.bus;

/**
 * Sub-phases for fine-grained event listener ordering within a priority level.
 * <p>
 * Each {@link EventPriority} can have multiple sub-phases to provide additional
 * ordering control when multiple listeners have the same priority.
 * </p>
 * <p>
 * Sub-phases are ordered: EARLIEST → EARLY → DEFAULT → LATE → LATEST
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * EventBus.subscribe(PlayerTickEvent.class)
 *     .priority(EventPriority.HIGH)
 *     .phase(EventPhase.EARLY)
 *     .listen(event -> { ... });
 * }</pre>
 * </p>
 *
 * @see EventPriority
 * @see EventBus
 * @since 1.0.0
 */
public enum EventPhase {

	/**
	 * Earliest phase — first to execute within a priority level.
	 * <p>
	 * Use for listeners that must run before any other listener
	 * at the same priority.
	 * </p>
	 */
	EARLIEST(0),

	/**
	 * Early phase — executes after EARLIEST but before DEFAULT.
	 */
	EARLY(1),

	/**
	 * Default phase — standard execution order.
	 * <p>
	 * This is the default if no phase is specified.
	 * </p>
	 */
	DEFAULT(2),

	/**
	 * Late phase — executes after DEFAULT but before LATEST.
	 */
	LATE(3),

	/**
	 * Latest phase — last to execute within a priority level.
	 * <p>
	 * Use for listeners that need to see the final state
	 * after all other same-priority listeners have run.
	 * </p>
	 */
	LATEST(4);

	/** The numeric phase value (lower = earlier execution). */
	private final int phase;

	/**
	 * Constructs a phase.
	 *
	 * @param phase the numeric phase value
	 */
	EventPhase(int phase) {
		this.phase = phase;
	}

	/**
	 * Returns the numeric phase value.
	 * <p>
	 * Lower values represent earlier execution.
	 * </p>
	 *
	 * @return the phase value
	 */
	public int getPhase() {
		return phase;
	}
}
