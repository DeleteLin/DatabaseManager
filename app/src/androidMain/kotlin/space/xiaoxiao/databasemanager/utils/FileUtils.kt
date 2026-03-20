package space.xiaoxiao.databasemanager.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.coroutines.resume

/**
 * Android 平台文件工具实现
 * 使用简单的回调机制处理文件选择
 */
actual object FileUtils {
    private var activityRef: Activity? = null

    // 文件选择回调
    private var pickFileCallback: ((String?) -> Unit)? = null
    private var saveFileCallback: ((Boolean) -> Unit)? = null
    private var pendingSaveContent: String? = null
    private var pendingSaveBytes: ByteArray? = null

    // 请求码
    private const val REQUEST_CODE_PICK_FILE = 1001
    private const val REQUEST_CODE_SAVE_FILE = 1002

    /**
     * 初始化文件工具，需要在 Activity 中调用
     */
    fun init(activity: Activity) {
        activityRef = activity
    }

    /**
     * 处理 Activity 结果，需要在 Activity.onActivityResult 中调用
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        when (requestCode) {
            REQUEST_CODE_PICK_FILE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri = data.data
                    if (uri != null) {
                        val content = readFileContent(activityRef!!, uri)
                        pickFileCallback?.invoke(content)
                    } else {
                        pickFileCallback?.invoke(null)
                    }
                } else {
                    pickFileCallback?.invoke(null)
                }
                pickFileCallback = null
                return true
            }
            REQUEST_CODE_SAVE_FILE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri = data.data
                    if (uri != null) {
                        val success = when {
                            pendingSaveContent != null -> {
                                writeTextContent(activityRef!!, uri, pendingSaveContent!!)
                            }
                            pendingSaveBytes != null -> {
                                writeBinaryContent(activityRef!!, uri, pendingSaveBytes!!)
                            }
                            else -> false
                        }
                        saveFileCallback?.invoke(success)
                    } else {
                        saveFileCallback?.invoke(false)
                    }
                } else {
                    saveFileCallback?.invoke(false)
                }
                saveFileCallback = null
                pendingSaveContent = null
                pendingSaveBytes = null
                return true
            }
        }
        return false
    }

    actual suspend fun pickFile(extensions: List<String>): String? = suspendCancellableCoroutine { continuation ->
        val activity = activityRef
        if (activity == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        pickFileCallback = { content ->
            continuation.resume(content)
        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            // 仅当指定扩展名时限制 MIME；空列表表示不限制后缀，避免系统筛选掉非 sql/txt 等文件
            if (extensions.isNotEmpty()) {
                putExtra(Intent.EXTRA_MIME_TYPES, extensions.map { ext ->
                    when (ext.lowercase()) {
                        "sql" -> "application/sql"
                        "txt" -> "text/plain"
                        "csv" -> "text/csv"
                        "json" -> "application/json"
                        else -> "application/octet-stream"
                    }
                }.toTypedArray())
            }
        }

        activity.startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
    }

    actual suspend fun saveFile(content: String, defaultName: String, extension: String): Boolean = suspendCancellableCoroutine { continuation ->
        val activity = activityRef
        if (activity == null) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        saveFileCallback = { success ->
            continuation.resume(success)
        }

        pendingSaveContent = content
        pendingSaveBytes = null

        val fileName = if (defaultName.contains(".")) defaultName else "$defaultName.$extension"
        val mimeType = when (extension.lowercase()) {
            "sql" -> "application/sql"
            "txt" -> "text/plain"
            "csv" -> "text/csv"
            "json" -> "application/json"
            else -> "application/octet-stream"
        }

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        activity.startActivityForResult(intent, REQUEST_CODE_SAVE_FILE)
    }

    actual suspend fun saveBinaryFile(bytes: ByteArray, defaultName: String, extension: String): Boolean = suspendCancellableCoroutine { continuation ->
        val activity = activityRef
        if (activity == null) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        saveFileCallback = { success ->
            continuation.resume(success)
        }

        pendingSaveContent = null
        pendingSaveBytes = bytes

        val fileName = if (defaultName.contains(".")) defaultName else "$defaultName.$extension"
        val mimeType = when (extension.lowercase()) {
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "xls" -> "application/vnd.ms-excel"
            else -> "application/octet-stream"
        }

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        activity.startActivityForResult(intent, REQUEST_CODE_SAVE_FILE)
    }

    private fun readFileContent(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun writeTextContent(context: Context, uri: Uri, content: String): Boolean {
        return try {
            if (content.isEmpty()) return false
            val outputStream = context.contentResolver.openOutputStream(uri) ?: return false
            val bytes = content.toByteArray(Charsets.UTF_8)
            if (bytes.isEmpty()) return false
            outputStream.use { stream ->
                stream.write(bytes)
                stream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun writeBinaryContent(context: Context, uri: Uri, bytes: ByteArray): Boolean {
        return try {
            if (bytes.isEmpty()) return false
            val outputStream = context.contentResolver.openOutputStream(uri) ?: return false
            outputStream.use { stream ->
                stream.write(bytes)
                stream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}