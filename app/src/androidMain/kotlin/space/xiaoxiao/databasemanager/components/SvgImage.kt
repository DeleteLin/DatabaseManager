package space.xiaoxiao.databasemanager.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import space.xiaoxiao.databasemanager.core.DatabaseType

/**
 * Android 平台的 SVG 图标加载实现
 * 使用 Coil 库加载 SVG 资源
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
 * Android 平台的 SVG Logo 加载实现
 */
@Composable
actual fun SvgLogo(
    logo: DatabaseLogo,
    modifier: Modifier,
    contentDescription: String?
) {
    val context = LocalContext.current

    val assetPath = when (logo) {
        DatabaseLogo.MYSQL -> "file:///android_asset/composeResources/databasemanagerworkspace.app.generated.resources/drawable/logo_mysql.svg"
        DatabaseLogo.POSTGRESQL -> "file:///android_asset/composeResources/databasemanagerworkspace.app.generated.resources/drawable/logo_postgresql.svg"
        DatabaseLogo.REDIS -> "file:///android_asset/composeResources/databasemanagerworkspace.app.generated.resources/drawable/logo_redis.svg"
        DatabaseLogo.DATABASES -> "file:///android_asset/composeResources/databasemanagerworkspace.app.generated.resources/drawable/logo_databases.svg"
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(assetPath)
            .build(),
        imageLoader = imageLoader,
        contentDescription = contentDescription ?: logo.name,
        modifier = modifier
    )
}

/**
 * Android 平台的国旗图标加载实现
 */
@Composable
actual fun SvgCountryFlag(
    flag: CountryFlag,
    modifier: Modifier,
    contentDescription: String?
) {
    val context = LocalContext.current

    val assetPath = when (flag) {
        CountryFlag.CHINA -> "file:///android_asset/composeResources/databasemanagerworkspace.app.generated.resources/drawable/country_china.svg"
        CountryFlag.USA -> "file:///android_asset/composeResources/databasemanagerworkspace.app.generated.resources/drawable/country_usa.svg"
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(assetPath)
            .build(),
        imageLoader = imageLoader,
        contentDescription = contentDescription ?: flag.name,
        modifier = modifier
    )
}

/**
 * Android 平台的导航图标加载实现
 */
@Composable
actual fun SvgNavIcon(
    icon: NavIcon,
    modifier: Modifier,
    contentDescription: String?
) {
    val context = LocalContext.current

    val assetPath = when (icon) {
        NavIcon.DATABASE -> "file:///android_asset/composeResources/databasemanagerworkspace.app.generated.resources/drawable/nav_database.svg"
        NavIcon.TABLE -> "file:///android_asset/composeResources/databasemanagerworkspace.app.generated.resources/drawable/nav_table.svg"
        NavIcon.QUERY -> "file:///android_asset/composeResources/databasemanagerworkspace.app.generated.resources/drawable/nav_query.svg"
        NavIcon.CHART -> "file:///android_asset/composeResources/databasemanagerworkspace.app.generated.resources/drawable/nav_chart.svg"
        NavIcon.MORE -> "file:///android_asset/composeResources/databasemanagerworkspace.app.generated.resources/drawable/nav_more.svg"
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(assetPath)
            .build(),
        imageLoader = imageLoader,
        contentDescription = contentDescription ?: icon.name,
        modifier = modifier
    )
}