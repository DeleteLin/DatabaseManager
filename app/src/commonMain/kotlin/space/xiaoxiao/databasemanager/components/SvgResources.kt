package space.xiaoxiao.databasemanager.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import space.xiaoxiao.databasemanager.core.DatabaseType
import space.xiaoxiao.databasemanager.i18n.Language

/**
 * 数据库 Logo 类型
 */
enum class DatabaseLogo {
    MYSQL,
    POSTGRESQL,
    REDIS,
    DATABASES  // 通用数据库图标
}

/**
 * 国旗图标类型
 */
enum class CountryFlag {
    CHINA,
    USA
}

/**
 * 导航图标类型
 */
enum class NavIcon {
    DATABASE,
    TABLE,
    QUERY,
    CHART,
    MORE
}

/**
 * 跨平台 SVG 图标加载组件
 *
 * Android 使用 Coil 加载 SVG
 * JVM/Desktop 使用 painterResource 加载 SVG
 */
@Composable
expect fun SvgDatabaseIcon(
    databaseType: DatabaseType,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
)

/**
 * 跨平台 SVG Logo 加载组件
 *
 * 支持所有数据库相关的 Logo
 */
@Composable
expect fun SvgLogo(
    logo: DatabaseLogo,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
)

/**
 * 跨平台国旗图标加载组件
 */
@Composable
expect fun SvgCountryFlag(
    flag: CountryFlag,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
)

/**
 * 跨平台导航图标加载组件
 */
@Composable
expect fun SvgNavIcon(
    icon: NavIcon,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
)

/**
 * 根据语言获取对应国旗
 */
fun Language.toCountryFlag(): CountryFlag = when (this) {
    Language.CHINESE -> CountryFlag.CHINA
    Language.ENGLISH -> CountryFlag.USA
}