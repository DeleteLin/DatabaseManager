package space.xiaoxiao.databasemanager.features

import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import space.xiaoxiao.databasemanager.core.QueryResult
import java.io.ByteArrayOutputStream

/**
 * 构建 Excel 文件字节数组（Android 平台）
 */
actual fun buildExcelBytes(result: QueryResult): ByteArray? {
    return try {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Query Result")

        // 创建表头样式
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        val headerFont = workbook.createFont().apply {
            bold = true
        }
        headerStyle.setFont(headerFont)

        // 创建表头行
        val headerRow = sheet.createRow(0)
        result.columns.forEachIndexed { index, column ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(column.name)
            cell.cellStyle = headerStyle
        }

        // 创建数据行
        result.rows.forEachIndexed { rowIndex, row ->
            val dataRow = sheet.createRow(rowIndex + 1)
            row.values.forEachIndexed { colIndex, value ->
                val cell = dataRow.createCell(colIndex)
                when (value) {
                    null -> cell.setCellValue("NULL")
                    is Number -> cell.setCellValue(value.toDouble())
                    is Boolean -> cell.setCellValue(value)
                    else -> cell.setCellValue(value.toString())
                }
            }
        }

        // 自动调整列宽
        result.columns.forEachIndexed { index, _ ->
            sheet.autoSizeColumn(index)
        }

        // 转换为字节数组
        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()
        outputStream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
