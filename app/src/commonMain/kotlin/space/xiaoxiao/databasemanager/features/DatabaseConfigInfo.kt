package space.xiaoxiao.databasemanager.features

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import space.xiaoxiao.databasemanager.core.DatabaseType
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource

data class DatabaseConfigInfo(
    val id: String,
    val name: String,
    val type: DatabaseType,
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
    val charset: String? = null
) {
    fun toDatabaseConfig(): space.xiaoxiao.databasemanager.core.DatabaseConfig {
        return space.xiaoxiao.databasemanager.core.DatabaseConfig(
            type = type,
            host = host,
            port = port,
            database = database,
            username = username,
            password = password,
            charset = charset
        )
    }
}

// ==================== 数据库类型扩展函数 ====================

/**
 * 获取数据库类型显示名称
 */
@Composable
fun DatabaseType.getDisplayName(language: Language): String = when (this) {
    DatabaseType.MYSQL -> stringResource("db_type_mysql", language)
    DatabaseType.POSTGRESQL -> stringResource("db_type_postgresql", language)
    DatabaseType.REDIS -> stringResource("db_type_redis", language)
}

/**
 * 获取数据库类型对应的 Badge 容器颜色
 */
@Composable
fun DatabaseType.getBadgeContainerColor(): Color = when (this) {
    DatabaseType.MYSQL -> MaterialTheme.colorScheme.tertiaryContainer
    DatabaseType.POSTGRESQL -> MaterialTheme.colorScheme.secondaryContainer
    DatabaseType.REDIS -> MaterialTheme.colorScheme.primaryContainer
}

/**
 * 获取数据库类型对应的 Badge 内容颜色
 */
@Composable
fun DatabaseType.getBadgeContentColor(): Color = when (this) {
    DatabaseType.MYSQL -> MaterialTheme.colorScheme.onTertiaryContainer
    DatabaseType.POSTGRESQL -> MaterialTheme.colorScheme.onSecondaryContainer
    DatabaseType.REDIS -> MaterialTheme.colorScheme.onPrimaryContainer
}

/**
 * 获取数据库类型默认端口
 */
fun DatabaseType.getDefaultPort(): String = when (this) {
    DatabaseType.MYSQL -> "3306"
    DatabaseType.POSTGRESQL -> "5432"
    DatabaseType.REDIS -> "6379"
}