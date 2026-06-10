package api.recoil;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Client-specific initialization entrypoint for the Recoil mod.
 * <p>
 * This class handles all client-side initialization including:
 * </p>
 * <ul>
 *   <li>Event bus initialization</li>
 *   <li>Settings system startup</li>
 *   <li>Module manager initialization</li>
 *   <li>Command system registration</li>
 *   <li>Fabric event bridge setup</li>
 * </ul>
 * <p>
 * This entrypoint is registered in fabric.mod.json under the "client" entrypoint
 * and only runs when the game is running in client mode.
 * </p>
 *
 * @see ClientMod for common initialization
 * @see ModInfo for mod constants and metadata
 */
public class ClientModClient implements ClientModInitializer {

	/** Logger instance for client-specific initialization. */
	private static final Logger LOGGER = LogManager.getLogger(ModInfo.MOD_ID + "/Client");

	@Override
	public void onInitializeClient() {
		LOGGER.info("[{}] Initializing client components (v{})", ModInfo.MOD_NAME, ModInfo.VERSION);

		// Phase markers - will be implemented in subsequent phases
		LOGGER.debug("[{}] Event bus will be initialized in Phase 2", ModInfo.MOD_NAME);
		LOGGER.debug("[{}] Settings system will be initialized in Phase 4", ModInfo.MOD_NAME);
		LOGGER.debug("[{}] Module manager will be initialized in Phase 5", ModInfo.MOD_NAME);
		LOGGER.debug("[{}] Command system will be initialized in Phase 6", ModInfo.MOD_NAME);

		LOGGER.info("[{}] Client initialization complete", ModInfo.MOD_NAME);
	}
}
