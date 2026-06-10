package api.recoil.event.bus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for declarative event handler methods.
 * <p>
 * Methods annotated with this annotation will be automatically discovered
 * and registered when an object is passed to {@link EventBus#register(Object)}.
 * </p>
 * <p>
 * Requirements for annotated methods:
 * </p>
 * <ul>
 *   <li>Must have exactly one parameter (the event type)</li>
 *   <li>Parameter must extend {@link Event}</li>
 *   <li>May be private (will be made accessible via reflection)</li>
 *   <li>Should not throw exceptions (exceptions are caught and logged)</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MyModule {
 *     @EventHandler(priority = EventPriority.HIGH)
 *     public void onPlayerTick(PlayerTickEvent event) {
 *         // Handle player tick
 *     }
 *
 *     @EventHandler(priority = EventPriority.MONITOR)
 *     private void onEntityDeath(EntityDeathEvent event) {
 *         // Monitor entity deaths (read-only)
 *     }
 * }
 *
 * // Register all annotated methods:
 * EventBus.register(new MyModule());
 * }</pre>
 * </p>
 *
 * @see EventBus#register(Object)
 * @see EventPriority
 * @see EventPhase
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {

	/**
	 * The priority at which this handler should receive events.
	 * <p>
	 * Default is {@link EventPriority#NORMAL}.
	 * </p>
	 *
	 * @return the event priority
	 */
	EventPriority priority() default EventPriority.NORMAL;

	/**
	 * The sub-phase for fine-grained ordering within the priority.
	 * <p>
	 * Default is {@link EventPhase#DEFAULT}.
	 * </p>
	 *
	 * @return the event phase
	 */
	EventPhase phase() default EventPhase.DEFAULT;
}
