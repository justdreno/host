package api.recoil;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Common initialization entrypoint for the Recoil mod.
 * <p>
 * This class handles initialization logic that runs on both the client and server,
 * though Recoil is a client-side only mod. This entrypoint is registered in
 * fabric.mod.json under the "main" entrypoint.
 * </p>
 *
 * @see ClientModClient for client-specific initialization
 * @see ModInfo for mod constants and metadata
 */
public class ClientMod implements ModInitializer {

	/** Logger instance for the common mod initialization. */
	private static final Logger LOGGER = LogManager.getLogger(ModInfo.MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[{}] Initializing common components (v{})", ModInfo.MOD_NAME, ModInfo.VERSION);
		// Common initialization - currently minimal as this is a client-only mod
	}
}
