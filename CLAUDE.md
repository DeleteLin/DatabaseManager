# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## THE "ULTRATHINK" PROTOCOL

**TRIGGER:** When the user prompts **"ULTRATHINK"**:
*   **Maximum Depth:** You must engage in exhaustive, deep-level reasoning.
*   **Multi-Dimensional Analysis:** Analyze through every lens - technical, accessibility, scalability, user experience.
*   **Prohibition:** **NEVER** use surface-level logic. Dig deeper until the logic is irrefutable.

---

## 项目概述

跨平台数据库管理应用（Android + JVM/Desktop）

**技术栈**: Kotlin 2.1.0 + Compose Multiplatform 1.7.0 + Gradle 8.9
**版本**: 1.3.0
**支持数据库**: MySQL, PostgreSQL, Redis

## 项目结构

```
databaseManagerWorkSpace/
├── core/database-operations/    # 数据库操作接口
├── core/android-stub/           # Android 桩库
├── core/rpc-client/             # RPC 客户端
├── rpc/                         # RPC 服务端
└── app/                         # 应用主模块
    ├── src/commonMain/          # 共享代码
    ├── src/androidMain/         # Android 实现
    └── src/jvmMain/             # JVM/Desktop 实现
```

## 核心规范（必须遵守）

### 1. 国际化 (i18n)

所有 UI 文本通过 `stringResource(key, language)` 映射，新增字符串时同时添加中英文翻译。

```kotlin
@Composable
fun MyScreen(language: Language = Language.CHINESE) {
    Text(text = stringResource("my_key", language))
}
```

### 2. 主题系统

禁止硬编码颜色值，使用 `MaterialTheme.colorScheme`。

```kotlin
Text(color = MaterialTheme.colorScheme.onSurface)
```

### 3. UI 规范

- Material Design 3，移动设备优先
- 使用 `AppCard`、`AppSpacing` 组件
- 图标使用 `materialIconsExtended`

### 4. 导航规范

| 页面级别 | 底部导航 | 顶部导航 |
|---------|---------|---------|
| 一级页面 | ✓ | ✗ |
| 二级页面 | ✗ | ✓ (+ 返回) |

### 5. 禁用反射

项目规范严禁使用 Java 反射和 Kotlin 反射。

## 核心模块与代码架构

### core:database-operations（数据库核心层）

- **领域模型**：`DatabaseConfig`, `DatabaseType`, `QueryResult`, `ExecutionResult`, `TableSchema`, `ColumnDefinition` 等，作为 UI 与驱动之间的通用语言。
- **执行上下文**：`DbExecutionContext` 抽象连接会话，持有 `DatabaseConfig`、当前 database/schema 等信息。
  - JVM/JDBC 实现：`JdbcExecution`（基于 JDBC Connection + Statement）。
  - Redis 实现：`RedisExecution`（基于 Jedis 等客户端）。
- **执行器（无状态）**：`CommandExecutor` 负责在给定 `DbExecutionContext` + `DbExecutionChannel` 上执行命令：
  - `execute` / `executeQuery` / `executeUpdate` / `openCursor`
  - 支持 `ExecutionPurpose.USER / META / UTIL` 区分用户查询和元数据查询。
- **元数据能力**：
  - 关系型：`RelationalMetadataOperations`（表/索引/统计/事务等）。
  - 键值型：`KeyValueMetadataOperations`（keys/逻辑库/容量等）。
- **客户端门面**：`DatabaseClient` 作为 app 访问 core 的统一入口：
  - `DatabaseClient.Relational(context, commands, metadata)`
  - `DatabaseClient.KeyValue(context, commands, metadata)`
- **工厂**：`DatabaseOperationsFactory` 按 `DatabaseType` 组装：
  - 创建对应的 `DbExecutionContext`（如 `JdbcExecution` / `RedisExecution`）
  - 绑定 `CommandExecutor` 与元数据接口
  - 返回封装好的 `DatabaseClient`

### app（应用层）

- **状态与主题**: `ThemeState`, `ColorTheme`
- **国际化**: `LocalizationState`, `Language`
- **存储**: `SecureStorage`, `AppConfigStorage`, `DatabaseConfigStorage`
- **数据库会话与查询**: `DatabaseViewModel`
  - 持有当前 tab 对应的 `DatabaseClient` 与连接配置。
  - 通过 `ExecutionPurpose.USER` 执行普通 SQL/Redis 命令。
  - 通过 `RelationalMetadataOperations` / `KeyValueMetadataOperations` 加载表结构、索引、统计与 Redis keys。
- **页面**: 位于 `features/` 目录：
  - 连接与配置：`DatabaseConfigScreen`
  - 查询：`StyledQueryScreen`
  - 图表：`ChartScreen` / `ChartEditorScreen`
  - 关于与设置等：`AboutScreen` 等

## 常用命令

```bash
./gradlew :app:run           # 运行 JVM 桌面应用
./gradlew :app:assembleDebug # 构建 Android Debug
./gradlew :app:packageDev    # 构建 Desktop 应用
```