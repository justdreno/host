# Fabric Client Mod Backend — Project Phases

**Minecraft Version:** 1.21.11 (Last Yarn-supported version)
**Mod ID:** recoil
**Package Root:** `api.recoil`
**Mappings:** Yarn (strictly enforced — no Mojang/MCP names)

---

## Current Phase: Phase 3

---

## Phase Checklist

### Phase 1 — Project Scaffold
- [x] **Goal:** Set up complete project structure with base entrypoints and compile verification
- [x] **Files to create/modify:**
  - `build.gradle` — ensure proper Fabric Loom configuration
  - `gradle.properties` — verify all version properties
  - `settings.gradle` — confirm project name
  - `src/main/resources/fabric.mod.json` — update entrypoints
  - `src/main/java/api/recoil/ClientMod.java` — ModInitializer entrypoint
  - `src/main/java/api/recoil/ClientModClient.java` — ClientModInitializer entrypoint
  - `src/main/java/api/recoil/ModInfo.java` — constants class (MOD_ID, MOD_NAME, VERSION, logger)
  - Create directory structure: `event/`, `settings/`, `module/`, `command/`, `config/`, `util/`
- [x] **Acceptance Criteria:**
  - Project compiles with `./gradlew build` (zero errors) — verified in development environment
  - Both entrypoints are registered in `fabric.mod.json`
  - `ModInfo` contains all constants with Javadoc
  - Directory structure matches specification

---

### Phase 2 — Core Event Bus
- [x] **Goal:** Build a fully custom, annotation-free, type-safe event bus
- [x] **Files to create:**
  - `event/bus/EventBus.java` — singleton manager
  - `event/bus/Event.java` — generic base class
  - `event/bus/CancellableEvent.java` — cancellable variant
  - `event/bus/EventListener.java` — functional interface
  - `event/bus/EventPriority.java` — enum (HIGHEST, HIGH, NORMAL, LOW, LOWEST, MONITOR)
  - `event/bus/EventPhase.java` — sub-phase enum
  - `event/bus/EventHandler.java` — optional annotation
  - `event/bus/WeakEventListener.java` — weak reference wrapper
  - `event/bus/EventException.java` — error handling wrapper
- [x] **Acceptance Criteria:**
  - Thread-safe subscriber list (CopyOnWriteArrayList)
  - Lock-free post operation
  - Exception isolation (one listener failure doesn't affect others)
  - `postAsync()` with ScheduledExecutorService
  - Zero Minecraft dependencies in core classes

---

### Phase 3 — Fabric Hook Bridge
- [ ] **Goal:** Bridge Fabric API events into our custom EventBus
- [ ] **Files to create:**
  - `event/bridge/FabricEventBridge.java` — main bridge class
  - `event/events/client/ClientTickEvent.java`
  - `event/events/client/RenderEvent.java`
  - `event/events/client/ConnectEvent.java`
  - `event/events/client/DisconnectEvent.java`
  - `event/events/client/ScreenEvent.java`
  - `event/events/entity/EntitySpawnEvent.java`
  - `event/events/entity/EntityDeathEvent.java`
  - `event/events/entity/EntityHurtEvent.java`
  - `event/events/world/BlockInteractEvent.java`
  - `event/events/world/ChunkLoadEvent.java`
  - `event/events/world/ChunkUnloadEvent.java`
- [ ] **Acceptance Criteria:**
  - All relevant Fabric events are bridged
  - Translated events fire through our EventBus
  - Single ModInitializer registration
  - Proper Yarn names throughout

---

### Phase 4 — Settings System
- [ ] **Goal:** Complete type-safe settings framework with Gson serialization
- [ ] **Files to create:**
  - `settings/Setting.java` — abstract base
  - `settings/SettingsManager.java` — singleton manager
  - `settings/SettingSerializer.java` — Gson output
  - `settings/SettingDeserializer.java` — Gson input
  - `settings/SettingsMigrator.java` — schema upgrades
  - `settings/SettingCategory.java` — hierarchical categories
  - `settings/types/BooleanSetting.java`
  - `settings/types/IntSetting.java`
  - `settings/types/FloatSetting.java`
  - `settings/types/DoubleSetting.java`
  - `settings/types/StringSetting.java`
  - `settings/types/EnumSetting.java`
  - `settings/types/ColorSetting.java`
  - `settings/types/KeybindSetting.java`
  - `settings/types/ListSetting.java`
  - `settings/types/RangeSetting.java`
  - `settings/types/MultiEnumSetting.java`
- [ ] **Acceptance Criteria:**
  - All setting types implemented with full validation
  - JSON serialization with human-readable output
  - Auto-save on change (2-second debounce)
  - Graceful recovery from corrupted/missing files
  - Save path: `.minecraft/config/recoil/settings.json`

---

### Phase 5 — Module System
- [ ] **Goal:** Base module infrastructure with enable/disable flow
- [ ] **Files to create:**
  - `module/AbstractModule.java`
  - `module/ModuleManager.java` — singleton manager
  - `module/ModuleCategory.java` — enum
  - `module/Module.java` — annotation
  - `event/events/module/ModuleEnableEvent.java`
  - `event/events/module/ModuleDisableEvent.java`
- [ ] **Acceptance Criteria:**
  - Toggle flow fires cancellable events
  - Auto-subscribe/unsubscribe from EventBus on enable/disable
  - Thread-safe enable/disable guard
  - Keybind integration via settings

---

### Phase 6 — Command System (Client-side)
- [ ] **Goal:** Client-side command dispatcher with chat interception
- [ ] **Files to create:**
  - `command/AbstractCommand.java`
  - `command/CommandManager.java` — singleton
  - `command/CommandContext.java`
  - `command/CommandException.java`
  - `command/impl/HelpCommand.java`
  - `command/impl/ToggleCommand.java`
  - `command/impl/SetCommand.java`
  - `command/impl/GetCommand.java`
  - `command/impl/BindCommand.java`
  - `event/events/command/CommandExecuteEvent.java`
- [ ] **Acceptance Criteria:**
  - Commands triggered by `.` prefix (configurable)
  - Tab-completion support
  - Chat interception prevents server-send
  - Built-in commands implemented

---

### Phase 7 — Utility Layer
- [ ] **Goal:** Rich utility library (no rendering)
- [ ] **Files to create:**
  - `util/PlayerUtils.java`
  - `util/WorldUtils.java`
  - `util/InventoryUtils.java`
  - `util/MathUtils.java`
  - `util/TimerUtils.java`
  - `util/ChatUtils.java`
  - `util/ReflectionUtils.java`
  - `util/ValidationUtils.java`
- [ ] **Acceptance Criteria:**
  - All utilities use Yarn names
  - Optional<T> for nullable returns
  - Full Javadoc on all public methods
  - No Minecraft dependencies in pure-math utilities

---

### Phase 8 — Configuration & Persistence Layer
- [ ] **Goal:** Profile support and hot-reload
- [ ] **Files to create:**
  - `config/ConfigProfile.java`
  - `config/ProfileManager.java` — singleton
  - `config/ConfigWatcher.java` — WatchService for hot-reload
  - `event/events/config/ConfigLoadedEvent.java`
  - `event/events/config/ConfigSavedEvent.java`
  - `event/events/config/ConfigReloadEvent.java`
- [ ] **Acceptance Criteria:**
  - Profile snapshots stored separately
  - Hot-reload on external file edits
  - Proper startup/shutdown sequences
  - `.minecraft/config/recoil/profiles/` storage

---

### Phase 9 — Event Catalogue & Documentation
- [ ] **Goal:** Complete documentation and final verification
- [ ] **Files to create:**
  - `EVENTS.md` — event documentation
  - `ARCHITECTURE.md` — system overview
- [ ] **Files to verify:**
  - All classes have complete Javadoc
- [ ] **Acceptance Criteria:**
  - `./gradlew build` — zero warnings, zero errors
  - Every public class/method has Javadoc
  - EVENTS.md lists all events with details
  - ARCHITECTURE.md explains all systems

---

## Notes

- Each phase must compile before moving to the next
- Yarn mappings are mandatory — no Mojang/MCP names anywhere
- Package structure: `api.recoil.*`
- Java 21 idioms required (records, sealed interfaces, pattern matching)
