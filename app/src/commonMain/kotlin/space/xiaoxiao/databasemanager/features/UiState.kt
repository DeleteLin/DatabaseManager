package space.xiaoxiao.databasemanager.features

/**
 * UI 状态密封类 - 用于统一管理加载/成功/错误状态
 */
sealed class UiState<out T> {
    /**
     * 空闲状态 - 初始状态或操作完成后的状态
     */
    object Idle : UiState<Nothing>()

    /**
     * 加载中状态
     */
    object Loading : UiState<Nothing>()

    /**
     * 成功状态 - 包含数据
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * 错误状态 - 包含错误信息和可选的异常
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
        val errorCode: ErrorCode = ErrorCode.UNKNOWN
    ) : UiState<Nothing>()

    /**
     * 判断是否处于加载状态
     */
    val isLoading: Boolean get() = this is Loading

    /**
     * 判断是否成功
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * 判断是否失败
     */
    val isError: Boolean get() = this is Error

    /**
     * 获取数据（如果成功）
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * 获取错误信息（如果失败）
     */
    fun errorOrNull(): String? = (this as? Error)?.message

    /**
     * 映射成功数据
     */
    inline fun <R> map(transform: (T) -> R): UiState<R> = when (this) {
        is Idle -> Idle
        is Loading -> Loading
        is Success -> Success(transform(data))
        is Error -> this
    }

    /**
     * 在成功时执行操作
     */
    inline fun onSuccess(action: (T) -> Unit): UiState<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * 在错误时执行操作
     */
    inline fun onError(action: (String, Throwable?) -> Unit): UiState<T> {
        if (this is Error) action(message, throwable)
        return this
    }

    /**
     * 在加载时执行操作
     */
    inline fun onLoading(action: () -> Unit): UiState<T> {
        if (this is Loading) action()
        return this
    }

    companion object {
        /**
         * 创建成功状态
         */
        fun <T> success(data: T): UiState<T> = Success(data)

        /**
         * 创建错误状态
         */
        fun error(message: String, throwable: Throwable? = null, errorCode: ErrorCode = ErrorCode.UNKNOWN): UiState<Nothing> =
            Error(message, throwable, errorCode)

        /**
         * 创建加载状态
         */
        fun loading(): UiState<Nothing> = Loading

        /**
         * 创建空闲状态
         */
        fun idle(): UiState<Nothing> = Idle
    }
}

/**
 * 错误代码枚举
 */
enum class ErrorCode {
    // 连接相关错误
    CONNECTION_FAILED,
    CONNECTION_TIMEOUT,
    CONNECTION_REFUSED,
    AUTHENTICATION_FAILED,
    NETWORK_ERROR,

    // 查询相关错误
    QUERY_FAILED,
    QUERY_TIMEOUT,
    SYNTAX_ERROR,
    PERMISSION_DENIED,

    // 数据相关错误
    TABLE_NOT_FOUND,
    COLUMN_NOT_FOUND,
    DATABASE_NOT_FOUND,
    DATA_VALIDATION_ERROR,

    // 操作相关错误
    OPERATION_CANCELLED,
    OPERATION_TIMEOUT,
    INVALID_STATE,

    // 通用错误
    UNKNOWN
}

/**
 * 安全执行操作的辅助函数
 */
suspend inline fun <T> safeExecute(
    crossinline operation: suspend () -> Result<T>
): UiState<T> {
    return try {
        operation().fold(
            onSuccess = { UiState.success(it) },
            onFailure = { exception ->
                UiState.error(
                    message = exception.message ?: "未知错误",
                    throwable = exception,
                    errorCode = ErrorCode.UNKNOWN
                )
            }
        )
    } catch (e: Exception) {
        UiState.error(
            message = e.message ?: "操作失败",
            throwable = e
        )
    }
}

/**
 * 扩展函数：将 Result 转换为 UiState
 */
fun <T> Result<T>.toUiState(): UiState<T> = fold(
    onSuccess = { UiState.success(it) },
    onFailure = { UiState.error(it.message ?: "操作失败", it) }
)