package space.xiaoxiao.databasemanager.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 代码编辑器组件
 * 支持 SQL 语法高亮和行号显示
 */
@Composable
fun CodeEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    placeholder: String? = null,
    language: EditorLanguage = EditorLanguage.SQL,
    showLineNumbers: Boolean = true,
    textStyle: TextStyle = TextStyle.Default,
    colors: CodeEditorColors? = null
) {
    val materialColors = MaterialTheme.colorScheme

    // 根据主题选择颜色配置
    val editorColors = colors ?: remember(materialColors) {
        // 判断是否为深色主题（通过 surface 颜色亮度判断）
        val isDark = materialColors.surface.luminance() < 0.5f
        if (isDark) {
            CodeEditorColors.dark(
                onSurface = materialColors.onSurface,
                onSurfaceVariant = materialColors.onSurfaceVariant,
                primary = materialColors.primary,
                secondary = materialColors.secondary,
                tertiary = materialColors.tertiary,
                outline = materialColors.outline
            )
        } else {
            CodeEditorColors.light(
                onSurface = materialColors.onSurface,
                onSurfaceVariant = materialColors.onSurfaceVariant,
                primary = materialColors.primary,
                secondary = materialColors.secondary,
                tertiary = materialColors.tertiary,
                outline = materialColors.outline
            )
        }
    }

    // 计算行数
    val lineCount = remember(value.text) {
        if (value.text.isEmpty()) 1 else value.text.count { it == '\n' } + 1
    }

    // 生成高亮文本
    val highlightedText = remember(value.text, editorColors, language) {
        if (value.text.isEmpty()) {
            AnnotatedString("")
        } else {
            LanguageSupportFactory.getSupport(language).highlight(value.text, editorColors)
        }
    }

    // 行号宽度计算
    val lineNumberWidth = remember(lineCount) {
        val digits = if (lineCount < 10) 2
        else if (lineCount < 100) 3
        else if (lineCount < 1000) 4
        else 5
        (digits * 12 + 16).dp
    }

    // 滚动状态同步
    val scrollState = rememberScrollState()
    val lineScrollState = rememberScrollState()

    // 同步行号滚动
    LaunchedEffect(scrollState.value) {
        lineScrollState.scrollTo(scrollState.value)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(materialColors.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 行号区域
            if (showLineNumbers) {
                Box(
                    modifier = Modifier
                        .width(lineNumberWidth)
                        .fillMaxHeight()
                        .background(editorColors.lineNumberBackground)
                        .verticalScroll(lineScrollState)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        repeat(lineCount) { index ->
                            Text(
                                text = "${index + 1}",
                                style = TextStyle(
                                    color = editorColors.lineNumberStyle.color,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 编辑区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // 占位符
                if (value.text.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = materialColors.onSurfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // 文本编辑器
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(12.dp),
                    enabled = enabled,
                    readOnly = readOnly,
                    textStyle = textStyle.copy(
                        color = editorColors.textStyle.color
                    ),
                    cursorBrush = SolidColor(materialColors.primary),
                    visualTransformation = VisualTransformation { _ ->
                        // 使用高亮文本
                        androidx.compose.ui.text.input.TransformedText(
                            highlightedText,
                            androidx.compose.ui.text.input.OffsetMapping.Identity
                        )
                    },
                    decorationBox = { innerTextField ->
                        innerTextField()
                    }
                )
            }
        }

        // 禁用状态遮罩
        if (!enabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(materialColors.surface.copy(alpha = 0.5f))
            )
        }
    }
}

/**
 * 辅助扩展函数：计算颜色亮度
 */
private fun androidx.compose.ui.graphics.Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}