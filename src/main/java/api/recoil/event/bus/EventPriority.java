package api.recoil.event.bus;

/**
 * Priority levels for event listeners.
 * <p>
 * Listeners are invoked in order from HIGHEST to LOWEST. MONITOR listeners
 * are always invoked last and receive events regardless of cancellation state.
 * </p>
 * <p>
 * Priority order:
 * </p>
 * <ol>
 *   <li>{@link #HIGHEST} - First to receive events, can cancel</li>
 *   <li>{@link #HIGH} - Early access, can cancel</li>
 *   <li>{@link #NORMAL} - Default priority, can cancel</li>
 *   <li>{@link #LOW} - After most listeners, can cancel</li>
 *   <li>{@link #LOWEST} - Last before MONITOR, can cancel</li>
 *   <li>{@link #MONITOR} - Always receives events, cannot cancel</li>
 * </ol>
 * <p>
 * Use priorities to control the order of event processing:
 * </p>
 * <ul>
 *   <li>Use HIGHEST for security/validation checks</li>
 *   <li>Use HIGH for early transformations</li>
 *   <li>Use NORMAL for standard logic (default)</li>
 *   <li>Use LOW for logging or secondary processing</li>
 *   <li>Use LOWEST for cleanup operations</li>
 *   <li>Use MONITOR for read-only observation</li>
 * </ul>
 *
 * @see EventBus
 * @see EventPhase
 * @since 1.0.0
 */
public enum EventPriority {

	/**
	 * Highest priority — first to receive events.
	 * <p>
	 * Use for critical checks that must run before any other listener.
	 * Can cancel events.
	 * </p>
	 */
	HIGHEST(0, true),

	/**
	 * High priority — early access to events.
	 * <p>
	 * Use for listeners that need to process events before most others.
	 * Can cancel events.
	 * </p>
	 */
	HIGH(1, true),

	/**
	 * Normal priority — default for most listeners.
	 * <p>
	 * Use for standard event handling logic.
	 * Can cancel events.
	 * </p>
	 */
	NORMAL(2, true),

	/**
	 * Low priority — after most listeners.
	 * <p>
	 * Use for listeners that depend on other listeners' modifications.
	 * Can cancel events.
	 * </p>
	 */
	LOW(3, true),

	/**
	 * Lowest priority — last normal priority.
	 * <p>
	 * Use for cleanup or final modifications before MONITOR listeners.
	 * Can cancel events.
	 * </p>
	 */
	LOWEST(4, true),

	/**
	 * Monitor priority — observes final event state.
	 * <p>
	 * Always receives events, even if cancelled. Cannot cancel events.
	 * Use for logging, metrics, or read-only observation.
	 * </p>
	 */
	MONITOR(5, false);

	/** The numeric priority value (lower = higher priority). */
	private final int priority;

	/** Whether listeners at this priority can cancel events. */
	private final boolean canCancel;

	/**
	 * Constructs a priority level.
	 *
	 * @param priority the numeric priority value
	 * @param canCancel whether this priority can cancel events
	 */
	EventPriority(int priority, boolean canCancel) {
		this.priority = priority;
		this.canCancel = canCancel;
	}

	/**
	 * Returns the numeric priority value.
	 * <p>
	 * Lower values represent higher priority (invoked first).
	 * </p>
	 *
	 * @return the priority value
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Returns whether listeners at this priority can cancel events.
	 * <p>
	 * MONITOR priority returns false; all others return true.
	 * </p>
	 *
	 * @return true if cancellation is allowed, false otherwise
	 */
	public boolean canCancel() {
		return canCancel;
	}

	/**
	 * Returns the next higher priority, or this if already highest.
	 *
	 * @return the next higher priority
	 */
	public EventPriority higher() {
		int index = ordinal();
		return index > 0 ? values()[index - 1] : this;
	}

	/**
	 * Returns the next lower priority, or this if already lowest.
	 *
	 * @return the next lower priority
	 */
	public EventPriority lower() {
		int index = ordinal();
		int maxIndex = values().length - 1;
		return index < maxIndex ? values()[index + 1] : this;
	}
}
