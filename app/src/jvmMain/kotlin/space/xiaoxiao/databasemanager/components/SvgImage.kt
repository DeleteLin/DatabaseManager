package space.xiaoxiao.databasemanager.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import org.jetbrains.compose.resources.painterResource
import databasemanagerworkspace.app.generated.resources.Res
import databasemanagerworkspace.app.generated.resources.logo_mysql
import databasemanagerworkspace.app.generated.resources.logo_postgresql
import databasemanagerworkspace.app.generated.resources.logo_redis
import databasemanagerworkspace.app.generated.resources.logo_databases
import databasemanagerworkspace.app.generated.resources.country_china
import databasemanagerworkspace.app.generated.resources.country_usa
import databasemanagerworkspace.app.generated.resources.nav_database
import databasemanagerworkspace.app.generated.resources.nav_table
import databasemanagerworkspace.app.generated.resources.nav_query
import databasemanagerworkspace.app.generated.resources.nav_chart
import databasemanagerworkspace.app.generated.resources.nav_more
import space.xiaoxiao.databasemanager.core.DatabaseType

/**
 * JVM/Desktop 平台的 SVG 图标加载实现
 * 使用 Compose Resources 的 painterResource 加载 SVG
 */
@Composable
actual fun SvgDatabaseIcon(
    databaseType: DatabaseType,
    modifier: Modifier,
    contentDescription: String?
) {
    val logo = when (databaseType) {
        DatabaseType.MYSQL -> DatabaseLogo.MYSQL
        DatabaseType.POSTGRESQL -> DatabaseLogo.POSTGRESQL
        DatabaseType.REDIS -> DatabaseLogo.REDIS
    }
    SvgLogo(logo = logo, modifier = modifier, contentDescription = contentDescription)
}

/**
 * JVM/Desktop 平台的 SVG Logo 加载实现
 */
@Composable
actual fun SvgLogo(
    logo: DatabaseLogo,
    modifier: Modifier,
    contentDescription: String?
) {
    when (logo) {
        DatabaseLogo.MYSQL -> {
            Image(
                painter = painterResource(Res.drawable.logo_mysql),
                contentDescription = contentDescription ?: "MySQL",
                modifier = modifier
            )
        }
        DatabaseLogo.POSTGRESQL -> {
            Image(
                painter = painterResource(Res.drawable.logo_postgresql),
                contentDescription = contentDescription ?: "PostgreSQL",
                modifier = modifier
            )
        }
        DatabaseLogo.REDIS -> {
            Image(
                painter = painterResource(Res.drawable.logo_redis),
                contentDescription = contentDescription ?: "Redis",
                modifier = modifier
            )
        }
        DatabaseLogo.DATABASES -> {
            Image(
                painter = painterResource(Res.drawable.logo_databases),
                contentDescription = contentDescription ?: "Database",
                modifier = modifier
            )
        }
    }
}

/**
 * JVM/Desktop 平台的国旗图标加载实现
 */
@Composable
actual fun SvgCountryFlag(
    flag: CountryFlag,
    modifier: Modifier,
    contentDescription: String?
) {
    when (flag) {
        CountryFlag.CHINA -> {
            Image(
                painter = painterResource(Res.drawable.country_china),
                contentDescription = contentDescription ?: "China",
                modifier = modifier
            )
        }
        CountryFlag.USA -> {
            Image(
                painter = painterResource(Res.drawable.country_usa),
                contentDescription = contentDescription ?: "USA",
                modifier = modifier
            )
        }
    }
}

/**
 * JVM/Desktop 平台的导航图标加载实现
 */
@Composable
actual fun SvgNavIcon(
    icon: NavIcon,
    modifier: Modifier,
    contentDescription: String?
) {
    when (icon) {
        NavIcon.DATABASE -> {
            Image(
                painter = painterResource(Res.drawable.nav_database),
                contentDescription = contentDescription ?: "Database",
                modifier = modifier
            )
        }
        NavIcon.TABLE -> {
            Image(
                painter = painterResource(Res.drawable.nav_table),
                contentDescription = contentDescription ?: "Table",
                modifier = modifier
            )
        }
        NavIcon.QUERY -> {
            Image(
                painter = painterResource(Res.drawable.nav_query),
                contentDescription = contentDescription ?: "Query",
                modifier = modifier
            )
        }
        NavIcon.CHART -> {
            Image(
                painter = painterResource(Res.drawable.nav_chart),
                contentDescription = contentDescription ?: "Chart",
                modifier = modifier
            )
        }
        NavIcon.MORE -> {
            Image(
                painter = painterResource(Res.drawable.nav_more),
                contentDescription = contentDescription ?: "More",
                modifier = modifier
            )
        }
    }
}