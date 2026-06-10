package api.recoil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Central constants class for the Recoil mod.
 * <p>
 * Contains all mod metadata, identifiers, and configuration constants.
 * All constants are declared as {@code public static final} for global access.
 * </p>
 * <p>
 * Recommended usage: Import statically or reference via {@code ModInfo.CONSTANT_NAME}.
 * </p>
 *
 * @since 1.0.0
 */
public final class ModInfo {

	// ==================== Mod Metadata ====================

	/**
	 * The unique identifier for this mod.
	 * <p>
	 * Used for:
	 * </p>
	 * <ul>
	 *   <li>Configuration file paths</li>
	 *   <li>Logger namespace</li>
	 *   <li>Resource location prefixes</li>
	 *   <li>Translation key prefixes</li>
	 * </ul>
	 */
	public static final String MOD_ID = "recoil";

	/**
	 * Human-readable display name of the mod.
	 */
	public static final String MOD_NAME = "Recoil";

	/**
	 * Current version string of the mod.
	 * <p>
	 * Format: MAJOR.MINOR.PATCH (e.g., "1.0.0")
	 * </p>
	 */
	public static final String VERSION = "1.0.0";

	/**
	 * The target Minecraft version this mod is built for.
	 * <p>
	 * Note: 1.21.11 is the last Minecraft version with official Yarn mappings support.
	 * </p>
	 */
	public static final String MINECRAFT_VERSION = "1.21.11";

	/**
	 * The minimum Java version required to run this mod.
	 */
	public static final int JAVA_VERSION = 21;

	// ==================== Package Paths ====================

	/**
	 * Root package for all mod classes.
	 */
	public static final String ROOT_PACKAGE = "api.recoil";

	/**
	 * Package for event-related classes.
	 */
	public static final String EVENT_PACKAGE = ROOT_PACKAGE + ".event";

	/**
	 * Package for settings-related classes.
	 */
	public static final String SETTINGS_PACKAGE = ROOT_PACKAGE + ".settings";

	/**
	 * Package for module-related classes.
	 */
	public static final String MODULE_PACKAGE = ROOT_PACKAGE + ".module";

	/**
	 * Package for command-related classes.
	 */
	public static final String COMMAND_PACKAGE = ROOT_PACKAGE + ".command";

	/**
	 * Package for configuration-related classes.
	 */
	public static final String CONFIG_PACKAGE = ROOT_PACKAGE + ".config";

	/**
	 * Package for utility classes.
	 */
	public static final String UTIL_PACKAGE = ROOT_PACKAGE + ".util";

	// ==================== Configuration Paths ====================

	/**
	 * Configuration directory name relative to .minecraft.
	 */
	public static final String CONFIG_DIR_NAME = MOD_ID;

	/**
	 * Main settings file name.
	 */
	public static final String SETTINGS_FILE_NAME = "settings.json";

	/**
	 * Profiles subdirectory name.
	 */
	public static final String PROFILES_DIR_NAME = "profiles";

	// ==================== Logger ====================

	/**
	 * Primary logger instance for the mod.
	 * <p>
	 * Uses Log4j2 via Fabric's logging infrastructure.
	 * All log messages will be prefixed with the mod ID.
	 * </p>
	 */
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	// ==================== Timing Constants ====================

	/**
	 * Default debounce delay for auto-save operations in milliseconds.
	 * <p>
	 * Prevents excessive disk writes when multiple settings change rapidly.
	 * </p>
	 */
	public static final long AUTO_SAVE_DEBOUNCE_MS = 2000L;

	/**
	 * Default timeout for async event posting in milliseconds.
	 */
	public static final long ASYNC_EVENT_TIMEOUT_MS = 5000L;

	// ==================== Command Constants ====================

	/**
	 * Default command prefix for client commands.
	 * <p>
	 * Commands are triggered by chat messages starting with this character.
	 * </p>
	 */
	public static final char DEFAULT_COMMAND_PREFIX = '.';

	// ==================== Priority Constants ====================

	/**
	 * Number of priority levels in the event system.
	 * <p>
	 * Ordered: HIGHEST (0), HIGH (1), NORMAL (2), LOW (3), LOWEST (4), MONITOR (5)
	 * </p>
	 */
	public static final int EVENT_PRIORITY_COUNT = 6;

	// ==================== Constructor ====================

	/**
	 * Private constructor to prevent instantiation.
	 * <p>
	 * This class contains only static constants and should not be instantiated.
	 * </p>
	 */
	private ModInfo() {
		throw new AssertionError("ModInfo is a constants class and cannot be instantiated");
	}
}
