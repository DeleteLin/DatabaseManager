# features/ — Screen-Level Composables & ViewModels

36 Kotlin files containing every screen, dialog, ViewModel, and storage helper in the app. All composables are in `commonMain` and shared between Android and Desktop.

## OVERVIEW
The largest source directory in the project. Contains screens (12 composable "Screen" files), dialogs (5), ViewModels (5), storage classes (5), and utilities. Navigation dispatches here via `NavDestination` enum in `AppNavigation.kt`.

## STRUCTURE
```
features/
├── StyledDatabaseListScreen.kt    # Database list (tables/keyspaces)
├── StyledTableBrowserScreen.kt    # Table data browser (1289 lines, LARGEST)
├── StyledQueryScreen.kt           # SQL query editor + results (957 lines)
├── DatabaseManageScreen.kt        # Database create/manage (554 lines)
├── DatabaseConfigScreen.kt        # Connection config editor (429 lines)
├── AiConfigScreen.kt              # AI model settings
├── AiSqlGeneratorDialog.kt        # AI-powered SQL generation (490 lines)
├── MoreScreen.kt                  # Settings hub (nav to others)
├── ConfigBackupScreen.kt          # Import/export config backup
├── DesignSystemScreen.kt          # UI component gallery (741 lines)
├── AboutScreen.kt                 # App version/info
├── SplashScreen.kt                # Launch splash with 2s delay (Compose, not native)
├── QueryTab.kt                    # Query tab model
├── QueryTabManager.kt             # Tab group manager
├── QueryResultTable.kt            # Result set table composable
├── TableCreateDialog.kt           # CREATE TABLE dialog
├── TableFieldEditor.kt            # Field definition editor
├── CreateTabDialog.kt             # New tab dialog
├── IndexCreateDialog.kt           # CREATE INDEX dialog
├── TemplateSelectorDialog.kt      # SQL template picker
├── FieldEditorScreen.kt           # Single-field value editor
├── DatabaseViewModel.kt           # GOD CLASS (703 lines): connection, query, table, column, index, history
├── BrowserViewModel.kt            # Table browser state
├── ConnectionViewModel.kt         # Connection form state
├── QueryViewModel.kt              # Query editor + result state
├── SchemaEditorViewModel.kt       # Schema editor state
├── DatabaseConfigStorage.kt       # Connection config persistence (expect)
├── DatabaseConfigStorageHelper.kt # JSON serialization helper
├── QueryHistoryStorage.kt         # SQL/Redis history (expect)
├── QueryHistoryStorageHelper.kt   # History serialization helper
├── QuerySessionStorage.kt         # Session restore (expect)
├── DatabaseConfigInfo.kt          # Connection config data class
├── BackupService.kt               # Config backup export/import
├── DataExport.kt                  # CSV/JSON export
├── ExcelExport.kt                 # Excel export via POI (expect)
├── UiState.kt                     # UiState sealed class
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Add screen | New file + add to `NavDestination` enum in AppNavigation.kt | Follow `Styled*Screen.kt` naming |
| Add dialog | New file + call from parent screen | Pass `onDismiss` + `onConfirm` callbacks |
| Add ViewModel | New file | Use `mutableStateOf` for now; migrate to StateFlow |
| Connection flow | `ConnectionViewModel.kt` → `DatabaseConfigStorage.kt` | Form validation → JSON persist |
| Query execution | `QueryViewModel.kt` → `styledQueryScreen` | SQL input → DatabaseOperations.executeQuery |
| Table browser | `BrowserViewModel.kt` → `StyledTableBrowserScreen.kt` | Pagination, filter, sort, CRUD |
| Schema editing | `SchemaEditorViewModel.kt` → `TableCreateDialog.kt` → `FieldEditorScreen.kt` | Column types, constraints |
| Data export | `DataExport.kt` → `ExcelExport.kt` | CSV/JSON/Excel formats |
| Config backup | `ConfigBackupScreen.kt` → `BackupService.kt` | Full config export/import |

## CONVENTIONS

- **Screen naming**: `Styled*Screen.kt` or `*Screen.kt`. Screens are `@Composable` functions, not classes.
- **Dialog naming**: `*Dialog.kt`. Dialogs are `@Composable` functions, typically `@OptIn(ExperimentalMaterial3Api::class)`.
- **ViewModel naming**: `*ViewModel.kt`. State holders with `mutableStateOf` for Compose observation.
- **Storage naming**: `*Storage.kt` for persistence interfaces (often `expect`/`actual` for platform impls).
- **Helper naming**: `*Helper.kt` for serialization/processing utilities.
- **Navigation**: Each screen has a corresponding `NavDestination` enum value. Screens receive navigation callbacks as lambdas.
- **Parameter passing**: Screens receive dependencies explicitly via function parameters. No DI framework.
- **Back navigation**: Manual — each screen handles its own back button or onBackPress callback.

## ANTI-PATTERNS

- **DO NOT** add state to `DatabaseViewModel` (703 lines, 23+ `mutableStateOf`) — create a focused ViewModel instead
- **DO NOT** expose passwords — `DatabaseConfigInfo.toString()` must NOT include `password` field
- **DO NOT** use `Jetpack Navigation` (NavHost/NavController) — the project uses manual `when()` routing; keep it consistent
- **DO NOT** import platform-specific APIs in commonMain — use `expect`/`actual` in `utils/` or `storage/` instead
- **NEVER** ignore the language param — all user-facing strings must go through `i18n/stringResource()`
