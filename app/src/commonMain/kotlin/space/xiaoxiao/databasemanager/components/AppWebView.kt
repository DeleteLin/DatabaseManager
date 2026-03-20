package space.xiaoxiao.databasemanager.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 跨平台 WebView 组件
 *
 * - Android：内嵌 WebView 加载网页
 * - JVM/Desktop：不强依赖内嵌 WebView，可降级为“打开网页”
 */
@Composable
expect fun AppWebView(
    url: String,
    modifier: Modifier = Modifier
)

