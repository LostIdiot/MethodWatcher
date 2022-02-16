package cn.zz.xposed.methodwatcher.data

sealed class DataResult<out T> {

    data class Success<out T>(val data: T) : DataResult<T>()

    data class Error(val exception: Exception) : DataResult<Nothing>()
}

inline fun <T, R> DataResult<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (exception: Exception) -> R
): R {
    return when (this) {
        is DataResult.Success -> onSuccess(this.data)
        is DataResult.Error -> onFailure(this.exception)
    }
}

class DataException(cause: Throwable? = null, message: String? = null) : Exception(message, cause)