package space.xiaoxiao.databasemanager.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.PrintWriter
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.JOptionPane

/**
 * JVM/Desktop 平台文件工具实现
 * 使用 JFileChooser 进行文件选择和保存
 */
actual object FileUtils {
    actual suspend fun pickFile(extensions: List<String>): String? = withContext(Dispatchers.IO) {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "选择文件"
            isMultiSelectionEnabled = false
            fileSelectionMode = JFileChooser.FILES_ONLY

            // 添加文件过滤器
            if (extensions.isNotEmpty()) {
                val description = extensions.joinToString(", ").uppercase() + " 文件"
                fileFilter = FileNameExtensionFilter(description, *extensions.toTypedArray())
            }
        }

        val result = fileChooser.showOpenDialog(null)

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                fileChooser.selectedFile.readText(Charsets.UTF_8)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    actual suspend fun saveFile(content: String, defaultName: String, extension: String): Boolean = withContext(Dispatchers.IO) {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "保存文件"
            selectedFile = File(if (defaultName.contains(".")) defaultName else "$defaultName.$extension")

            // 添加文件过滤器
            if (extension.isNotEmpty()) {
                val description = extension.uppercase() + " 文件"
                fileFilter = FileNameExtensionFilter(description, extension)
            }
        }

        val result = fileChooser.showSaveDialog(null)

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                var file = fileChooser.selectedFile
                // 确保文件有扩展名
                if (!file.name.contains(".") && extension.isNotEmpty()) {
                    file = File(file.parentFile, "${file.name}.$extension")
                }

                // 如果文件存在，询问是否覆盖
                if (file.exists()) {
                    val confirm = JOptionPane.showConfirmDialog(
                        null,
                        "文件已存在，是否覆盖？",
                        "确认覆盖",
                        JOptionPane.YES_NO_OPTION
                    )
                    if (confirm != JOptionPane.YES_OPTION) {
                        return@withContext false
                    }
                }

                file.writeText(content, Charsets.UTF_8)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            false
        }
    }

    actual suspend fun saveBinaryFile(bytes: ByteArray, defaultName: String, extension: String): Boolean = withContext(Dispatchers.IO) {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "保存文件"
            selectedFile = File(if (defaultName.contains(".")) defaultName else "$defaultName.$extension")

            // 添加文件过滤器
            if (extension.isNotEmpty()) {
                val description = extension.uppercase() + " 文件"
                fileFilter = FileNameExtensionFilter(description, extension)
            }
        }

        val result = fileChooser.showSaveDialog(null)

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                var file = fileChooser.selectedFile
                // 确保文件有扩展名
                if (!file.name.contains(".") && extension.isNotEmpty()) {
                    file = File(file.parentFile, "${file.name}.$extension")
                }

                // 如果文件存在，询问是否覆盖
                if (file.exists()) {
                    val confirm = JOptionPane.showConfirmDialog(
                        null,
                        "文件已存在，是否覆盖？",
                        "确认覆盖",
                        JOptionPane.YES_NO_OPTION
                    )
                    if (confirm != JOptionPane.YES_OPTION) {
                        return@withContext false
                    }
                }

                file.writeBytes(bytes)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            false
        }
    }
}