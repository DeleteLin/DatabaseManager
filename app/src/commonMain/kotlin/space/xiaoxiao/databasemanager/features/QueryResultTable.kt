package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.core.QueryResult
import space.xiaoxiao.databasemanager.i18n.stringResource

/**
 * 查询结果表格 - 使用虚拟滚动优化大数据量性能
 */
@Composable
fun QueryResultTable(
    result: QueryResult,
    modifier: Modifier = Modifier,
    language: Language = Language.CHINESE,
    onExportCsv: (() -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current

    // 列宽范围
    val minColumnWidth = 80.dp
    val maxColumnWidth = 200.dp

    Column(modifier = modifier) {
        // 使用 LazyColumn 实现虚拟滚动
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            // 表头 - 使用 item 而非 stickyHeader 以保持简单
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    result.columns.forEach { column ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .widthIn(min = minColumnWidth, max = maxColumnWidth)
                                .height(40.dp)
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(column.name))
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = column.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 数据行 - 使用 items 实现虚拟化
            items(result.rows) { row ->
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    row.values.forEach { value ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .widthIn(min = minColumnWidth, max = maxColumnWidth)
                                .height(40.dp)
                                .clickable {
                                    value?.let {
                                        clipboardManager.setText(AnnotatedString(it.toString()))
                                    }
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.CenterStart,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = value?.toString() ?: "NULL",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (value == null)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 导出 CSV 按钮
        if (onExportCsv != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onExportCsv,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource("export_csv", language))
            }
        }
    }
}

/**
 * 构建简单的表格视图（用于小数据量）
 */
@Composable
fun SimpleQueryResultTable(
    result: QueryResult,
    modifier: Modifier = Modifier,
    language: Language = Language.CHINESE,
    onExportCsv: (() -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current

    // 列宽范围
    val minColumnWidth = 80.dp
    val maxColumnWidth = 200.dp

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
    ) {
        // 表头
        Row {
            result.columns.forEachIndexed { index, column ->
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .widthIn(min = minColumnWidth, max = maxColumnWidth)
                        .height(40.dp)
                        .clickable {
                            clipboardManager.setText(AnnotatedString(column.name))
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = column.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 数据行
        result.rows.forEachIndexed { rowIndex, row ->
            Row {
                row.values.forEachIndexed { colIndex, value ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .widthIn(min = minColumnWidth, max = maxColumnWidth)
                            .height(40.dp)
                            .clickable {
                                value?.let {
                                    clipboardManager.setText(AnnotatedString(it.toString()))
                                }
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = value?.toString() ?: "NULL",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (value == null)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 导出 CSV 按钮
        if (onExportCsv != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onExportCsv,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource("export_csv", language))
            }
        }
    }
}

/**
 * 构建 CSV 字符串
 */
fun buildCsvString(result: QueryResult): String {
    val sb = StringBuilder()

    // 添加表头
    sb.append(result.columns.joinToString(",") { it.name })
    sb.append("\n")

    // 添加数据行
    result.rows.forEach { row ->
        val values = row.values.map { value ->
            value?.toString()?.let {
                // 处理包含逗号、引号或换行符的值
                if (it.contains(",") || it.contains("\"") || it.contains("\n")) {
                    "\"${it.replace("\"", "\"\"")}\""
                } else {
                    it
                }
            } ?: "NULL"
        }
        sb.append(values.joinToString(","))
        sb.append("\n")
    }

    return sb.toString()
}