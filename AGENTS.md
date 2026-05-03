# PROJECT KNOWLEDGE BASE

**Generated:** 2026-05-03
**Commit:** 33b6902
**Branch:** main

## OVERVIEW
Cross-platform database manager (MySQL, PostgreSQL, Redis) built with Kotlin Multiplatform + Compose Multiplatform. Targets Android (minSdk 27) and JVM Desktop.

## STRUCTURE
```
./
├── app/                          # Main application (KMP: android + jvm)
│   └── src/
│       ├── commonMain/           # Shared UI + business logic (~90% of code)
│       ├── androidMain/          # Android boot (MainActivity), platform stubs
│       ├── jvmMain/              # Desktop boot (Main.kt), platform stubs
│       └── jvmTest/              # Compose UI tests (JUnit4)
├── core/
│   ├── database-operations/      # DB abstraction layer (KMP: android + jvm)
│   │   └── src/
│   │       ├── commonMain/       # DB interfaces (DatabaseOperations, MetadataOperations)
│   │       ├── jvmAndroidMain/   # JDBC impl (MySQL, PG) + Redis — SHARED between JVM targets
│   │       ├── androidMain/      # Android-specific factory (registers platform drivers)
│   │       ├── jvmMain/          # JVM-specific factory
│   │       └── jvmTest/          # JUnit5 + H2 in-memory DB
│   └── android-stub/             # Stub classes for JDK APIs missing on Android
├── gradle/                       # Version catalog (libs.versions.toml), wrapper
├── keystore/                     # Release signing (gitignored)
├── scripts/                      # Manual build/install scripts (not CI)
└── docs/images/                  # Screenshots
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Add a new screen | `app/src/commonMain/.../features/` | One file per screen, follow existing patterns |
| Add a UI component | `app/src/commonMain/.../components/` | Reusable @Composable functions |
| Add chart type | `app/src/commonMain/.../charts/` | ChartScreen, ChartEditorScreen, ChartPanelManager |
| Change database logic | `core/database-operations/src/` | Interfaces in commonMain, impl in jvmAndroidMain |
| Add DB driver support | `core/database-operations/src/jvmAndroidMain/` | JdbcDrivers.kt registers drivers |
| Add theme/color | `app/src/commonMain/.../theme/` | ThemeState, colors/ subdirectory |
| Add translations | `app/src/commonMain/.../i18n/StringResources.kt` | key-value map, auto-generated |
| Add storage/config | `app/src/commonMain/.../storage/` | JSON-serialized configs via kotlinx.serialization |
| Android-specific code | `app/src/androidMain/` | ActivityResult, WebView, SecureStorage, keystore |
| Desktop-specific code | `app/src/jvmMain/` | Window setup, JvmSecureStorage |
| ProGuard rules | `app/proguard-rules.pro` | JDBC driver keep rules (broad!) |
| Version catalog | `gradle/libs.versions.toml` | All dep/plugin versions (some unused entries) |

## CODE MAP

| Symbol | Type | Location | Refs | Role |
|--------|------|----------|------|------|
| `App()` | @Composable | commonMain/App.kt | 2 (MainActivity, Main.kt) | Root composable, splash→main routing |
| `AppNavigation` | @Composable | commonMain/AppNavigation.kt | 1 | Manual nav host via NavDestination enum + when() |
| `MainActivity` | Activity | androidMain/MainActivity.kt | 1 (manifest) | Android entry, wires deps via remember{} |
| `Main.kt` | main() | jvmMain/Main.kt | 1 (build.gradle) | Desktop entry, mirrors MainActivity |
| `DatabaseOperations` | interface | core/commonMain/DatabaseOperations.kt | — | Core DB abstraction |
| `JdbcDatabaseOperations` | class | core/jvmAndroidMain/JdbcDatabaseOperations.kt | — | MySQL/PostgreSQL JDBC impl (1128 lines) |
| `RedisDatabaseOperations` | class | core/jvmAndroidMain/RedisDatabaseOperations.kt | — | Redis Jedis impl |
| `DatabaseViewModel` | class | features/DatabaseViewModel.kt | features/* | God class: connection+query+table+column+index+history (703 lines) |
| `ThemeState` | class | theme/ThemeState.kt | — | Color theme + dark mode via StateFlow |
| `LocalizationState` | class | i18n/LocalizationState.kt | — | Chinese/English language state |
| `NavDestination` | enum | AppNavigation.kt | 11 screens | Manual routing destinations (no Jetpack Navigation) |

## CONVENTIONS

- **Kotlin code style**: `kotlin.code.style=official` (no custom codestyle XMLs or .editorconfig)
- **Dependency management**: `gradle/libs.versions.toml` for versioned deps (except JDBC drivers, POI, Jedis — hardcoded inline)
- **State management**: Inconsistent — `mutableStateOf` in DatabaseViewModel vs `StateFlow` everywhere else. Prefer StateFlow going forward.
- **Platform-specific code**: `expect`/`actual` for platform APIs (FileUtils, SecureStorage, EncryptionManager, AppExit). NOT in commonMain.
- **Navigation**: Manual `NavDestination` enum + `when()` in AppNavigation. No Jetpack Navigation Compose (no NavHost, no NavController).
- **DI**: Manual constructor injection via `remember{}` blocks. No Hilt, Koin, or Dagger. No Application subclass.
- **Resources**: Compose Multiplatform resource format (`composeResources/`), not `res/values/`.
- **String resources**: Declared in `i18n/StringResources.kt` as key-value map, not in XML.
- **Error handling**: `runCatching {}` + `.onFailure {}` pattern in commonMain. No unified error model.

## ANTI-PATTERNS (THIS PROJECT)

- **DO NOT** expose passwords in `toString()` — DatabaseConfig/DatabaseConfigInfo must NOT log/print connection strings
- **DO NOT** add more MySQL/PG if-else branching in JdbcDatabaseOperations — extract to SqlDialect strategy
- **DO NOT** add more responsibilities to DatabaseViewModel (703 lines, 23+ mutableStateOf, 25+ suspend functions) — split into focused ViewModels
- **DO NOT** use `afterEvaluate` for build logic — migrate keystore guard to task dependencies
- **DO NOT** add new JDBC driver dependency without testing minSdk=27 compatibility (PostgreSQL requires MethodHandle API)
- **DO NOT** forget ProGuard keep rules when adding JDBC/reflection-dependent libraries
- **NEVER** commit `keystore/keystore.properties` or `keystore/*.jks` — they're gitignored
- **ALWAYS** create AGENTS.md child files when adding a new Gradle module or source directory with >15 distinct files

## UNIQUE STYLES

- **jvmAndroidMain source set**: Custom intermediate KMP source set shared between Android and JVM in `core/database-operations`. Tooling may not recognize it.
- **android-stub module**: Provides JVM-only JDK class stubs so JDBC drivers compile on Android. Fragile — driver version changes may break stubs.
- **Splash via Compose**: `SplashScreen.kt` with `LaunchedEffect { delay(2000) }` instead of native splash API. May show blank frame briefly.
- **singleInstance launch mode**: Isolates app to single task. Blocks external intent handling.
- **Broad ProGuard rules**: `-keep class com.mysql.cj.** { *; }` keeps entire MySQL connector — increases APK size.

## COMMANDS
```bash
./gradlew :app:run                        # Run JVM desktop app
./gradlew :app:assembleDebug              # Build Android debug APK
./gradlew :app:assembleRelease            # Build Android release APK (needs keystore)
./gradlew :core:database-operations:jvmTest  # Run JUnit5 tests (H2 in-memory DB)
./gradlew :app:packageDev                 # Package desktop app (.deb)
```

## NOTES

- **minSdk=27** (Android 8.1) is mandatory — PostgreSQL JDBC driver requires `java.lang.invoke.MethodHandle`. Do NOT lower without replacing the driver.
- **AGP 8.5.2 / Kotlin 2.1.0 / Compose 1.7.0 / Gradle 8.9** — pinned versions, upgrade all four together.
- **No CI/CD** — no GitHub Actions, Jenkins, or any pipeline. All builds are manual via shell scripts or Gradle CLI.
- **No code quality tooling** — no detekt, ktlint, .editorconfig, or Android Lint config. Only `Project_Default.xml` IDE inspections.
- **Unused version catalog entries**: ktor-server-* (4 entries), ktor-client-darwin, ktor-client-java, koin-*, datastore-preferences. Clean up before adding new deps.
- **Duplicate plugin alias**: `kotlin-serialization` and `serialization` both point to `org.jetbrains.kotlin.plugin.serialization` in version catalog. `serialization` is dead.
- **Zero `.java` files** — project is 100% Kotlin.
- **iOS target** exists as empty `iosMain` directories but is disabled (`kotlin.native.ignoreDisabledTargets=true`).
