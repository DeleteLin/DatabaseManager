# core/database-operations — Database Abstraction Layer

KMP library providing typed interfaces for relational (MySQL, PostgreSQL) and key-value (Redis) database operations. Shared between Android and JVM Desktop.

## OVERVIEW
22 Kotlin files across 4 KMP source sets. JDBC-based implementation for MySQL/PostgreSQL (shared via custom `jvmAndroidMain` source set). Jedis-based implementation for Redis. JUnit5 tests with H2 in-memory DB.

## STRUCTURE
```
src/
├── commonMain/              # Interfaces: DatabaseOperations, MetadataOperations, DbCommand, SqlDialect, DatabaseFeature
├── jvmAndroidMain/          # JDBC impl (MySQL+PG) + Redis — SHARED between android/jvm
│   ├── JdbcDatabaseOperations.kt   # 1128 lines: connection, query, table/column/index admin
│   ├── JdbcExecution.kt            # SQL execution + ResultSet parsing
│   ├── JdbcTableHandler.kt         # Table list/schema reading
│   ├── JdbcTableAdminHandler.kt    # CREATE/ALTER/DROP table
│   ├── JdbcColumnHandler.kt        # Column metadata
│   ├── JdbcIndexHandler.kt         # Index operations
│   ├── JdbcTransactionHandler.kt   # Transaction BEGIN/COMMIT/ROLLBACK
│   ├── JdbcDatabaseAdminHandler.kt # Database CREATE/DROP
│   ├── JdbcDrivers.kt              # Driver registration (MySQL, PG, Redis)
│   ├── JdbcUtils.kt                # Connection string building
│   ├── RedisDatabaseOperations.kt  # Redis Jedis impl
│   ├── RedisExecution.kt           # Redis command execution
│   ├── RedisDriver.kt              # Redis connection
│   └── DatabaseOperationsFactory.kt
├── androidMain/             # Android-specific factory (platform driver init)
├── jvmMain/                 # JVM-specific factory (platform driver init)
└── jvmTest/                 # JUnit5 tests (H2 in-memory, test containers)
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Add new SQL operation | `jvmAndroidMain/JdbcExecution.kt` | ResultSet parsing logic |
| Add query method | `jvmAndroidMain/JdbcDatabaseOperations.kt` | Query construction + execution |
| Add schema operation | `jvmAndroidMain/JdbcTableAdminHandler.kt` | CREATE/ALTER/DROP |
| Add DB driver | `jvmAndroidMain/JdbcDrivers.kt` | Register + classloading |
| Change interface | `commonMain/DatabaseOperations.kt` | Must update all impls |
| Add tests | `jvmTest/` | JUnit5 + `@Test` + H2 |
| Platform-specific init | `androidMain/` or `jvmMain/` | Driver classloading differs |

## CONVENTIONS

- **Interface in commonMain, impl in jvmAndroidMain** — db ops are defined as interfaces in `commonMain/`, implemented once in `jvmAndroidMain/`, and wired by a thin factory per platform
- **SqlDialect enum** (MySQL, PostgreSQL) drives behavior branching — **prefer adding dialect methods over if-else checks**
- **Error handling**: Exceptions propagate to caller. No wrapping or domain error types.
- **Connection config**: `DatabaseConfig` data class with `dbType`, `host`, `port`, `user`, `password`, `databaseName`
- **Result format**: `DbCommandResult` with `columns: List<ColumnMeta>`, `rows: List<Map<String, Any?>>`
- **Column types**: `ColumnMeta` with `name`, `type`, `nullable`, `isPrimaryKey`, `defaultValue`, `isAutoIncrement`
- **Test DB**: H2 for relational, no Redis tests

## ANTI-PATTERNS

- **DO NOT** add more MySQL/PG if-else branching in `JdbcDatabaseOperations` — use `SqlDialect` methods instead
- **DO NOT** add new methods to `DatabaseOperations` interface without implementing in all backing classes
- **DO NOT** add JDBC driver dependencies with versions hardcoded — use version catalog
- **DO NOT** change `jvmAndroidMain` source set name — tooling (IDE, Gradle plugins) may not recognize arbitrary names
- **NEVER** hardcode connection strings — always build from `DatabaseConfig`
- **ALWAYS** register new platform drivers in `JdbcDrivers.kt` via `Class.forName()`
