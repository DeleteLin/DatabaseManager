package space.xiaoxiao.databasemanager.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.net.URI

/**
 * JVM/Desktop：Compose Desktop 未内置 WebView；这里做降级，提供“打开网页”按钮。
 */
@Composable
actual fun AppWebView(
    url: String,
    modifier: Modifier
) {
    val canBrowse = remember {
        try {
            Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
        } catch (_: Exception) {
            false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Changelog",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = url,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (canBrowse) {
            Button(
                onClick = {
                    try {
                        Desktop.getDesktop().browse(URI(url))
                    } catch (_: Exception) {
                    }
                },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(text = "Open")
            }
        } else {
            Text(
                text = "Platform does not support open URL.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

