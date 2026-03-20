package space.xiaoxiao.databasemanager.features

import space.xiaoxiao.databasemanager.core.QueryResult

/**
 * 构建 Excel 文件字节数组
 * JVM 平台支持，Android 平台返回 null
 */
expect fun buildExcelBytes(result: QueryResult): ByteArray?