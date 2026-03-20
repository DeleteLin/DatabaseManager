package space.xiaoxiao.databasemanager.i18n

import androidx.compose.runtime.Composable

/**
 * 字符串资源映射
 * 按功能模块分组，便于维护和查找
 */

/**
 * 获取字符串资源
 * @param key 资源键名
 * @param language 当前语言
 * @return 对应语言的字符串，如果不存在则返回 key 本身
 */
@Composable
fun stringResource(key: String, language: Language = Language.CHINESE): String {
    val resources = if (language == Language.CHINESE) chineseStringResources else englishStringResources
    return resources[key] ?: key
}

/**
 * 中文字符串资源映射
 */
val chineseStringResources = mapOf(
    // 应用
    "app_name" to "小小数据库",
    "splash_title" to "小小数据库",

    // 导航
    "nav_database_list" to "数据库",
    "nav_table_browser" to "表浏览",
    "nav_query" to "查询",
    "nav_chart" to "图表",

    // 图表模块
    "chart_type" to "图表类型",
    "bar_chart" to "柱状图",
    "pie_chart" to "饼图",
    "line_chart" to "折线图",
    "execute_chart" to "生成图表",
    "chart_no_data" to "请先执行查询",
    "chart_select_database" to "请先选择数据库",
    "chart_no_result" to "查询结果为空",
    "chart_label_column" to "标签列",
    "chart_value_column" to "数值列",
    // 图表面板
    "chart_panel" to "图表面板",
    "new_chart_panel" to "新建面板",
    "rename_panel" to "重命名面板",
    "delete_panel" to "删除面板",
    "panel_name" to "面板名称",
    "no_chart_panels" to "暂无图表面板",
    "add_chart_panel_hint" to "点击 + 创建新面板",
    "confirm_delete_panel" to "确定要删除面板",
    // 图表配置
    "chart_title" to "图表标题",
    "chart_description" to "图表描述",
    "add_chart" to "添加图表",
    "edit_chart" to "编辑图表",
    "delete_chart" to "删除图表",
    "chart_width" to "图表尺寸",
    "chart_preview" to "图表预览",
    "chart_color" to "图表颜色",
    "chart_size_small" to "小",
    "chart_size_medium" to "中",
    "chart_size_large" to "大",
    "chart_size_full" to "全宽",
    "no_charts" to "暂无图表",
    "add_chart_hint" to "点击右下角按钮添加图表",
    "preview" to "预览",
    "create" to "创建",
    "nav_more" to "更多",
    "nav_profile" to "设置",

    // 通用
    "design_system" to "UI 规范",
    "ok" to "确定",
    "cancel" to "取消",
    "save" to "保存",
    "delete" to "删除",
    "edit" to "编辑",
    "add" to "添加",
    "loading" to "加载中...",
    "error" to "错误",
    "success" to "成功",
    "no_data" to "暂无数据",
    "more" to "更多",

    // 配置备份（更多 -> 二级页面）
    "config_backup" to "配置备份",
    "config_backup_desc" to "导入/导出全部配置（含密码与密钥），需要密码加密",
    "export_config" to "导出配置",
    "import_config" to "导入配置",
    "clear_config_and_exit" to "清空配置并退出",
    "config_backup_export_detail" to "导出为加密文件，可在其他设备导入",
    "config_backup_import_detail" to "从加密备份文件恢复全部配置",
    "config_backup_clear_detail" to "删除所有配置并关闭应用",
    "enter_backup_password" to "请输入备份密码",
    "confirm_backup_password" to "再次输入密码",
    "password_mismatch" to "两次输入的密码不一致",
    "password_too_short" to "密码至少需要 {min} 位",
    "wrong_password_or_corrupted" to "密码错误或文件已损坏",
    "confirm_clear_config_exit" to "确定要清空所有配置并关闭应用吗？此操作不可逆！",

    // 语言
    "language_chinese" to "中文",
    "language_english" to "英语",
    "language_settings" to "语言设置",

    // 用户与统计
    "user" to "用户",
    "statistics" to "统计",
    "databases" to "数据库",
    "query_count" to "查询次数",

    // 主题
    "theme_settings" to "主题设置",
    "color_theme_settings" to "颜色主题",
    "theme_light" to "浅色",
    "theme_dark" to "深色",
    "theme_green" to "绿色",
    "theme_blue" to "蓝色",
    "theme_purple" to "紫色",
    "theme_pink" to "粉色",
    "theme_slate_orange" to "橙彩",
    "theme_dark_black" to "暗黑",

    // 关于
    "about" to "关于",

    // 数据库列表
    "empty_database_list" to "暂无数据库连接",
    "add_database_hint" to "点击右下角 + 按钮添加数据库",
    "confirm_delete_database" to "确认删除数据库",
    "delete_database_warning" to "确定要删除此数据库连接吗？",
    "delete_session_warning" to "关联的查询会话数据也将被删除",
    "delete" to "删除",

    // SQL 查询
    "sql_statement" to "SQL 语句",
    "sql_placeholder" to "在此输入 SQL 语句...",
    "execute" to "执行",
    "query_result" to "查询结果",
    "query_result_placeholder" to "执行 SQL 查询后显示结果",

    // 查询会话
    "new_query_session" to "新建查询会话",
    "session_name" to "会话名称",
    "session_name_placeholder" to "例如：生产环境查询",

    // 数据库配置
    "add_database" to "添加数据库",
    "edit_database" to "编辑数据库",
    "connection_name" to "连接名称",
    "connection_name_placeholder" to "例如：生产环境 MySQL",
    "database_type" to "数据库类型",
    "host" to "主机",
    "port" to "端口",
    "database_name" to "数据库名",
    "database_name_placeholder" to "例如：mydb",
    "username" to "用户名",
    "username_placeholder" to "例如：root",
    "password" to "密码",
    "password_placeholder" to "请输入密码",
    "show_password" to "显示密码",
    "hide_password" to "隐藏密码",
    "test_connection" to "测试连接",
    "connecting" to "连接中...",
    "connected" to "已连接",
    "disconnected" to "未连接",
    "connection_failed" to "连接失败",
    "select_database" to "选择数据库",
    "no_database_selected" to "未选择数据库",
    "no_database_configured" to "请先配置数据库连接",

    // 字符集
    "charset" to "字符集",
    "charset_utf8mb4" to "UTF-8 MB4 (推荐)",
    "charset_utf8" to "UTF-8",
    "charset_latin1" to "Latin1",
    "charset_gbk" to "GBK",
    "charset_utf8_pg" to "UTF-8",
    "charset_latin1_pg" to "Latin1",
    "charset_gbk_pg" to "GBK",
    "select_charset" to "选择字符集",

    // 历史记录
    "query_history" to "历史记录",

    // 表浏览器
    "database_label" to "数据库",
    "current_table" to "当前表",
    "not_selected" to "未选择",
    "click_to_select_table" to "点击选择表",
    "failed" to "失败",
    "table_schema" to "表结构",
    "refresh" to "刷新",
    "select_table_to_show_data" to "选择表后显示数据",
    "select_table" to "选择表",
    "no_tables" to "暂无表",
    "table_schema_title" to "表结构：{tableName}",
    "column_info" to "列信息",
    "not_null" to "非空",
    "default_value" to "默认值：{value}",
    "comment" to "注释：{comment}",
    "primary_key" to "主键",
    "total_rows" to "共 {rowCount} 行",
    "execution_time" to "执行时间：{ms}ms",
    "pk_badge" to "PK",
    "ai_badge" to "自增",

    // 查询屏幕
    "error_with_message" to "错误：{message}",
    "close" to "关闭",
    "query_failed" to "查询失败：{error}",

    // 数据库类型
    "db_type_mysql" to "MySQL",
    "db_type_postgresql" to "PostgreSQL",
    "db_type_redis" to "Redis",

    // 重试
    "retry" to "重试",

    // 多标签页
    "new_tab" to "新建标签页",
    "close_tab" to "关闭标签页",
    "no_tab" to "没有打开的标签页",
    "no_tab_hint" to "点击上方 + 按钮创建新标签页",

    // 表管理
    "create_table" to "新建表",
    "drop_table" to "删除表",
    "rename_table" to "重命名表",
    "truncate_table" to "清空表",
    "table_name" to "表名",
    "table_structure" to "表结构",
    "table_actions" to "表操作",

    // 字段管理
    "fields" to "字段",
    "add_field" to "添加字段",
    "edit_field" to "编辑字段",
    "delete_field" to "删除字段",
    "field_name" to "字段名",
    "field_type" to "字段类型",
    "field_length" to "长度",
    "field_nullable" to "允许为空",
    "field_default" to "默认值",
    "field_comment" to "注释",
    "field_primary_key" to "主键",
    "field_auto_increment" to "自增",

    // 索引管理
    "indexes" to "索引",
    "create_index" to "创建索引",
    "drop_index" to "删除索引",
    "index_name" to "索引名",
    "index_columns" to "索引字段",
    "index_unique" to "唯一索引",
    "index_type" to "索引类型",

    // 统计信息
    "row_count" to "行数",
    "data_size" to "数据大小",
    "index_size" to "索引大小",
    "create_time" to "创建时间",
    "update_time" to "更新时间",

    // 外键
    "foreign_keys" to "外键",
    "fk_column" to "字段",
    "fk_referenced_table" to "引用表",
    "fk_referenced_column" to "引用字段",

    // 确认提示
    "confirm_drop_table" to "确定要删除表 {table} 吗？此操作不可恢复。",
    "confirm_drop_column" to "确定要删除字段 {column} 吗？",
    "confirm_drop_index" to "确定要删除索引 {index} 吗？",
    "confirm_truncate_table" to "确定要清空表 {table} 的所有数据吗？此操作不可恢复。",

    // 通用操作
    "search" to "搜索",
    "actions" to "操作",
    "type" to "类型",
    "name" to "名称",
    "nullable" to "可空",
    "unique" to "唯一",
    "primary" to "主键",
    "back" to "返回",
    "export_csv" to "导出 CSV",

    // 数据库切换
    "server_database_label" to "服务器数据库",
    "switch_database" to "切换数据库",
    "refresh_database_list" to "刷新数据库列表",
    "refresh_failed" to "刷新失败",
    "no_database_available" to "暂无数据库",
    "server_databases" to "服务器数据库列表",
    "current_database" to "当前数据库",

    // 数据库管理
    "manage_databases" to "管理数据库",
    "create_database" to "创建数据库",
    "drop_database" to "删除数据库",
    "database_name_hint" to "数据库名称",
    "confirm_drop_database" to "确定要删除数据库 {database} 吗？此操作不可逆！",
    "database_size" to "数据库大小",
    "table_count" to "表数量",
    "database_name_exists" to "数据库已存在",
    "database_created" to "数据库创建成功",
    "database_dropped" to "数据库已删除",
    "database_name_empty" to "数据库名称不能为空",
    "database_name_invalid" to "数据库名称包含非法字符",
    "redis_no_database_management" to "Redis 不支持动态数据库管理",

    // 查询页面
    "command_editor" to "命令编辑器",
    "redis_command_hint" to "输入 Redis 命令，如：GET mykey",
    "execute_selected" to "执行选中",
    "execute_all" to "执行全部",
    "auto_expand" to "自动展开",
    "collapse_result" to "折叠结果",
    "expand_result" to "展开结果",
    "execution_success" to "执行成功",
    "affected_rows" to "受影响行数：{rows}",

    // 文件导入导出
    "import_file" to "导入文件",
    "export_file" to "导出文件",
    "export_as_csv" to "导出为 CSV",
    "export_as_excel" to "导出为 Excel",
    "import_success" to "导入成功",
    "export_success" to "导出成功",
    "import_failed" to "导入失败",
    "export_failed" to "导出失败",
    "sql_files" to "SQL 文件",
    "text_files" to "文本文件",

    // AI 生成 SQL
    "sql_templates" to "常用语句模板",
    "ai_generate" to "AI 生成语句",
    "ai_config" to "AI 配置",
    "ai_config_menu" to "AI 配置",
    "ai_api_type" to "接口类型",
    "ai_base_url" to "接口地址",
    "ai_api_key" to "API 密钥",
    "ai_prompt" to "提示词",
    "ai_user_input" to "描述你的需求",
    "ai_generating" to "AI 生成中...",
    "ai_insert" to "插入到编辑器",
    "ai_config_required" to "请先配置 AI 接口",
    "ai_config_saved" to "AI 配置已保存",
    "ai_generate_success" to "生成成功",
    "ai_generate_failed" to "生成失败",
    "ai_retry" to "重试",
    "ai_close" to "关闭",
    "ai_select_template" to "选择模板",
    "template_select_hint" to "点击选择模板插入到编辑器",
    "template_common_sql" to "常用 SQL 语句",
    "ai_api_type_openai" to "OpenAI 兼容",
    "ai_api_type_claude" to "Claude 兼容",
    "ai_base_url_hint" to "例如：https://api.openai.com/v1",
    "ai_api_key_hint" to "请输入 API 密钥",
    "ai_prompt_hint" to "提示词支持占位符：{dbType}, {tableSchema}, {userInput}",
    "ai_user_input_hint" to "例如：查询所有用户的姓名和邮箱，按注册时间排序",
    "ai_reset_prompt" to "重置为默认提示词",
    "ai_confirm_reset" to "确定要重置提示词吗？",
    "ai_no_result" to "AI 未返回结果，请重试",
    "ai_please_go_to_more_page" to "请先在「更多」>「AI 配置」页面配置 AI 接口信息",
    "go_to_more_page" to "去配置"
)

/**
 * 英文字符串资源映射
 */
val englishStringResources = mapOf(
    // App
    "app_name" to "Database Manager",
    "splash_title" to "Tiny Database",

    // Navigation
    "nav_database_list" to "Databases",
    "nav_table_browser" to "Tables",
    "nav_query" to "Query",
    "nav_chart" to "Chart",

    // Chart module
    "chart_type" to "Chart Type",
    "bar_chart" to "Bar Chart",
    "pie_chart" to "Pie Chart",
    "line_chart" to "Line Chart",
    "execute_chart" to "Generate Chart",
    "chart_no_data" to "Please execute a query first",
    "chart_select_database" to "Please select a database",
    "chart_no_result" to "Query result is empty",
    "chart_label_column" to "Label Column",
    "chart_value_column" to "Value Column",
    // Chart Panel
    "chart_panel" to "Chart Panel",
    "new_chart_panel" to "New Panel",
    "rename_panel" to "Rename Panel",
    "delete_panel" to "Delete Panel",
    "panel_name" to "Panel Name",
    "no_chart_panels" to "No chart panels",
    "add_chart_panel_hint" to "Click + to create a new panel",
    "confirm_delete_panel" to "Are you sure you want to delete panel",
    // Chart Config
    "chart_title" to "Chart Title",
    "chart_description" to "Chart Description",
    "add_chart" to "Add Chart",
    "edit_chart" to "Edit Chart",
    "delete_chart" to "Delete Chart",
    "chart_width" to "Chart Size",
    "chart_preview" to "Chart Preview",
    "chart_color" to "Chart Color",
    "chart_size_small" to "Small",
    "chart_size_medium" to "Medium",
    "chart_size_large" to "Large",
    "chart_size_full" to "Full Width",
    "no_charts" to "No charts",
    "add_chart_hint" to "Click the button below to add a chart",
    "preview" to "Preview",
    "create" to "Create",
    "nav_more" to "More",
    "nav_profile" to "Settings",

    // Common
    "design_system" to "UI Specs",
    "ok" to "OK",
    "cancel" to "Cancel",
    "save" to "Save",
    "delete" to "Delete",
    "edit" to "Edit",
    "add" to "Add",
    "loading" to "Loading...",
    "error" to "Error",
    "success" to "Success",
    "no_data" to "No data",
    "more" to "More",

    // Config backup (More -> second-level page)
    "config_backup" to "Config Backup",
    "config_backup_desc" to "Import/export all configs (including passwords and keys) with password encryption",
    "export_config" to "Export Config",
    "import_config" to "Import Config",
    "clear_config_and_exit" to "Clear Config & Exit",
    "config_backup_export_detail" to "Export encrypted file, import on other devices",
    "config_backup_import_detail" to "Restore all configs from encrypted backup file",
    "config_backup_clear_detail" to "Delete all configs and close the app",
    "enter_backup_password" to "Enter backup password",
    "confirm_backup_password" to "Confirm password",
    "password_mismatch" to "Passwords do not match",
    "password_too_short" to "Password must be at least {min} characters",
    "wrong_password_or_corrupted" to "Wrong password or corrupted file",
    "confirm_clear_config_exit" to "Clear all configs and close the app? This cannot be undone!",

    // Language
    "language_chinese" to "Chinese",
    "language_english" to "English",
    "language_settings" to "Language",

    // User & Statistics
    "user" to "User",
    "statistics" to "Statistics",
    "databases" to "Databases",
    "query_count" to "Queries",

    // Theme
    "theme_settings" to "Theme",
    "color_theme_settings" to "Color Theme",
    "theme_light" to "Light",
    "theme_dark" to "Dark",
    "theme_green" to "Green",
    "theme_blue" to "Blue",
    "theme_purple" to "Purple",
    "theme_pink" to "Pink",
    "theme_slate_orange" to "Slate Orange",
    "theme_dark_black" to "Dark Black",

    // About
    "about" to "About",

    // Database List
    "empty_database_list" to "No database connections",
    "add_database_hint" to "Click + button to add a database",
    "confirm_delete_database" to "Confirm Delete Database",
    "delete_database_warning" to "Are you sure you want to delete this database connection?",
    "delete_session_warning" to "Associated query session data will also be deleted",
    "delete" to "Delete",

    // SQL Query
    "sql_statement" to "SQL Statement",
    "sql_placeholder" to "Enter SQL statement here...",
    "execute" to "Execute",
    "query_result" to "Query Result",
    "query_result_placeholder" to "Execute SQL query to display results",

    // Query Session
    "new_query_session" to "New Query Session",
    "session_name" to "Session Name",
    "session_name_placeholder" to "e.g., Production Query",

    // Database Config
    "add_database" to "Add Database",
    "edit_database" to "Edit Database",
    "connection_name" to "Connection Name",
    "connection_name_placeholder" to "e.g., Production MySQL",
    "database_type" to "Database Type",
    "host" to "Host",
    "port" to "Port",
    "database_name" to "Database Name",
    "database_name_placeholder" to "e.g., mydb",
    "username" to "Username",
    "username_placeholder" to "e.g., root",
    "password" to "Password",
    "password_placeholder" to "Enter password",
    "show_password" to "Show password",
    "hide_password" to "Hide password",
    "test_connection" to "Test Connection",
    "connecting" to "Connecting...",
    "connected" to "Connected",
    "disconnected" to "Disconnected",
    "connection_failed" to "Connection Failed",
    "select_database" to "Select Database",
    "no_database_selected" to "No database selected",
    "no_database_configured" to "Please configure a database connection first",

    // Charset
    "charset" to "Charset",
    "charset_utf8mb4" to "UTF-8 MB4 (Recommended)",
    "charset_utf8" to "UTF-8",
    "charset_latin1" to "Latin1",
    "charset_gbk" to "GBK",
    "charset_utf8_pg" to "UTF-8",
    "charset_latin1_pg" to "Latin1",
    "charset_gbk_pg" to "GBK",
    "select_charset" to "Select Charset",

    // History
    "query_history" to "History",

    // Table Browser
    "database_label" to "Database",
    "current_table" to "Current Table",
    "not_selected" to "Not Selected",
    "click_to_select_table" to "Click to select table",
    "failed" to "Failed",
    "table_schema" to "Table Schema",
    "refresh" to "Refresh",
    "select_table_to_show_data" to "Select a table to show data",
    "select_table" to "Select Table",
    "no_tables" to "No tables",
    "table_schema_title" to "Table Schema: {tableName}",
    "column_info" to "Column Info",
    "not_null" to "NOT NULL",
    "default_value" to "Default: {value}",
    "comment" to "Comment: {comment}",
    "primary_key" to "Primary Key",
    "total_rows" to "{rowCount} rows",
    "execution_time" to "Execution: {ms}ms",
    "pk_badge" to "PK",
    "ai_badge" to "AI",

    // Query Screen
    "error_with_message" to "Error: {message}",
    "close" to "Close",
    "query_failed" to "Query failed: {error}",

    // Database Types
    "db_type_mysql" to "MySQL",
    "db_type_postgresql" to "PostgreSQL",
    "db_type_redis" to "Redis",

    // Retry
    "retry" to "Retry",

    // Multi-Tab
    "new_tab" to "New Tab",
    "close_tab" to "Close Tab",
    "no_tab" to "No open tabs",
    "no_tab_hint" to "Click the + button above to create a new tab",

    // Table Management
    "create_table" to "Create Table",
    "drop_table" to "Drop Table",
    "rename_table" to "Rename Table",
    "truncate_table" to "Truncate Table",
    "table_name" to "Table Name",
    "table_structure" to "Table Structure",
    "table_actions" to "Table Actions",

    // Field Management
    "fields" to "Fields",
    "add_field" to "Add Field",
    "edit_field" to "Edit Field",
    "delete_field" to "Delete Field",
    "field_name" to "Field Name",
    "field_type" to "Field Type",
    "field_length" to "Length",
    "field_nullable" to "Nullable",
    "field_default" to "Default",
    "field_comment" to "Comment",
    "field_primary_key" to "Primary Key",
    "field_auto_increment" to "Auto Increment",

    // Index Management
    "indexes" to "Indexes",
    "create_index" to "Create Index",
    "drop_index" to "Drop Index",
    "index_name" to "Index Name",
    "index_columns" to "Columns",
    "index_unique" to "Unique",
    "index_type" to "Index Type",

    // Statistics
    "row_count" to "Row Count",
    "data_size" to "Data Size",
    "index_size" to "Index Size",
    "create_time" to "Create Time",
    "update_time" to "Update Time",

    // Foreign Keys
    "foreign_keys" to "Foreign Keys",
    "fk_column" to "Column",
    "fk_referenced_table" to "Referenced Table",
    "fk_referenced_column" to "Referenced Column",

    // Confirmation
    "confirm_drop_table" to "Are you sure you want to drop table {table}? This action cannot be undone.",
    "confirm_drop_column" to "Are you sure you want to drop column {column}?",
    "confirm_drop_index" to "Are you sure you want to drop index {index}?",
    "confirm_truncate_table" to "Are you sure you want to truncate table {table}? This action cannot be undone.",

    // Common Actions
    "search" to "Search",
    "actions" to "Actions",
    "type" to "Type",
    "name" to "Name",
    "nullable" to "Nullable",
    "unique" to "Unique",
    "primary" to "Primary",
    "back" to "Back",
    "export_csv" to "Export CSV",

    // Database Switch
    "server_database_label" to "Server Database",
    "switch_database" to "Switch Database",
    "refresh_database_list" to "Refresh Database List",
    "refresh_failed" to "Refresh failed",
    "no_database_available" to "No database available",
    "server_databases" to "Server Databases",
    "current_database" to "Current Database",

    // Database Management
    "manage_databases" to "Manage Databases",
    "create_database" to "Create Database",
    "drop_database" to "Drop Database",
    "database_name_hint" to "Database Name",
    "confirm_drop_database" to "Are you sure you want to drop database {database}? This action cannot be undone!",
    "database_size" to "Database Size",
    "table_count" to "Table Count",
    "database_name_exists" to "Database already exists",
    "database_created" to "Database created successfully",
    "database_dropped" to "Database dropped",
    "database_name_empty" to "Database name cannot be empty",
    "database_name_invalid" to "Database name contains invalid characters",
    "redis_no_database_management" to "Redis does not support dynamic database management",

    // Query Page
    "command_editor" to "Command Editor",
    "redis_command_hint" to "Enter Redis command, e.g., GET mykey",
    "execute_selected" to "Execute Selected",
    "execute_all" to "Execute All",
    "auto_expand" to "Auto Expand",
    "collapse_result" to "Collapse",
    "expand_result" to "Expand",
    "execution_success" to "Execution Successful",
    "affected_rows" to "Affected Rows: {rows}",

    // File Import/Export
    "import_file" to "Import File",
    "export_file" to "Export File",
    "export_as_csv" to "Export as CSV",
    "export_as_excel" to "Export as Excel",
    "import_success" to "Import successful",
    "export_success" to "Export successful",
    "import_failed" to "Import failed",
    "export_failed" to "Export failed",
    "sql_files" to "SQL Files",
    "text_files" to "Text Files",

    // AI SQL Generator
    "sql_templates" to "SQL Templates",
    "ai_generate" to "AI Generate SQL",
    "ai_config" to "AI Configuration",
    "ai_config_menu" to "AI Configuration",
    "ai_api_type" to "API Type",
    "ai_base_url" to "Base URL",
    "ai_api_key" to "API Key",
    "ai_prompt" to "Prompt",
    "ai_user_input" to "Describe Your Request",
    "ai_generating" to "Generating...",
    "ai_insert" to "Insert to Editor",
    "ai_config_required" to "Please configure AI API first",
    "ai_config_saved" to "AI configuration saved",
    "ai_generate_success" to "Generated successfully",
    "ai_generate_failed" to "Generation failed",
    "ai_retry" to "Retry",
    "ai_close" to "Close",
    "ai_select_template" to "Select Template",
    "template_select_hint" to "Click to select a template and insert into editor",
    "template_common_sql" to "Common SQL Statements",
    "ai_api_type_openai" to "OpenAI Compatible",
    "ai_api_type_claude" to "Claude Compatible",
    "ai_base_url_hint" to "e.g.: https://api.openai.com/v1",
    "ai_api_key_hint" to "Please enter API Key",
    "ai_prompt_hint" to "Prompt supports placeholders: {dbType}, {tableSchema}, {userInput}",
    "ai_user_input_hint" to "e.g.: Query all users' names and emails, sorted by registration date",
    "ai_reset_prompt" to "Reset to Default Prompt",
    "ai_confirm_reset" to "Are you sure you want to reset the prompt?",
    "ai_no_result" to "AI returned no result, please retry",
    "ai_please_go_to_more_page" to "Please configure AI API in More > AI Configuration page",
    "go_to_more_page" to "Go to Configure"
)