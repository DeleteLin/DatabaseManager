package space.xiaoxiao.databasemanager.features

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import space.xiaoxiao.databasemanager.components.DatabaseLogo
import space.xiaoxiao.databasemanager.components.SvgLogo
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource

/**
 * 启动屏组件
 * 显示应用 Logo 和标题，2 秒后自动导航到主界面
 */
@Composable
fun SplashScreen(
    language: Language = Language.CHINESE,
    onNavigateToMain: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "splash_alpha"
    )

    // 启动动画
    LaunchedEffect(Unit) {
        startAnimation = true
        // 2 秒后导航到主界面
        delay(2000)
        onNavigateToMain()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim)
        ) {
            // 图标容器
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                ) {
                    SvgLogo(
                        logo = DatabaseLogo.DATABASES,
                        modifier = Modifier.size(56.dp),
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource("splash_title", language),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}