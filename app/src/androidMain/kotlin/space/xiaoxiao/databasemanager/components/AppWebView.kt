package space.xiaoxiao.databasemanager.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun AppWebView(
    url: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            // 尽量在 WebView 内部处理链接，避免弹到系统浏览器
            webViewClient = WebViewClient()
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.destroy()
        }
    }

    LaunchedEffect(url) {
        webView.loadUrl(url)
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}

