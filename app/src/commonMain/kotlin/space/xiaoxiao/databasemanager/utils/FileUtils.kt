package space.xiaoxiao.databasemanager.utils

/**
 * 跨平台文件工具类
 * 提供文件选择和保存功能
 */
expect object FileUtils {
    /**
     * 选择文件并读取内容
     * @param extensions 允许的文件扩展名列表，如 listOf("sql", "txt")；**空列表**表示不限制类型（全部分组/全部文件）
     * @return 文件内容，如果用户取消则返回 null
     */
    suspend fun pickFile(extensions: List<String>): String?

    /**
     * 保存文本内容到文件
     * @param content 文件内容
     * @param defaultName 默认文件名
     * @param extension 文件扩展名
     * @return 是否保存成功
     */
    suspend fun saveFile(content: String, defaultName: String, extension: String): Boolean

    /**
     * 保存二进制数据到文件
     * @param bytes 文件字节数据
     * @param defaultName 默认文件名
     * @param extension 文件扩展名
     * @return 是否保存成功
     */
    suspend fun saveBinaryFile(bytes: ByteArray, defaultName: String, extension: String): Boolean
}