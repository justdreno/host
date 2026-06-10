/**
 * Concrete event classes for the Recoil event system.
 * <p>
 * Events are organized by category:
 * </p>
 * <ul>
 *   <li>{@code client} - Client lifecycle events (tick, render, connect, etc.)</li>
 *   <li>{@code entity} - Entity-related events (spawn, death, hurt, etc.)</li>
 *   <li>{@code world} - World-related events (block interact, chunk load, etc.)</li>
 *   <li>{@code module} - Module lifecycle events (enable, disable)</li>
 *   <li>{@code command} - Command execution events</li>
 *   <li>{@code config} - Configuration events (load, save, reload)</li>
 * </ul>
 *
 * @since 1.0.0
 */
package api.recoil.event.events;
