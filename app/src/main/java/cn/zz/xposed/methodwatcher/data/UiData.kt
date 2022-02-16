package cn.zz.xposed.methodwatcher.data

import androidx.annotation.FloatRange
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class UiData<out T> {

    data class Loading<T>(@FloatRange(from = 0.0, to = 1.0) val progress: Float = 0f) :
        UiData<T>()

    data class Success<T>(val data: T) : UiData<T>()

    data class Failure<T>(val message: String) :
        UiData<T>()

    object Empty: UiData<Nothing>()
}

inline fun <T> DataResult<T>.toUiData(
    errorMessage: (e: Exception) -> String = {
        (it as? DataException)?.message ?: "Error!"
    }
): UiData<T> {
    return fold(
        onSuccess = { UiData.Success(it) },
        onFailure = { UiData.Failure(errorMessage(it)) })
}

@OptIn(ExperimentalContracts::class)
inline fun <T> UiData<T>.ifSuccess(success: (T) -> Unit) {
    contract {
        callsInPlace(success, InvocationKind.AT_MOST_ONCE)
    }
    if (this is UiData.Success) {
        success(this.data)
    }
}

fun <T> UiData<T>.getOrDefault(default: T): T {
    return (this as? UiData.Success)?.data ?: default
}

@OptIn(ExperimentalContracts::class)
inline fun <T> UiData<T>.getOrElse(default: () -> T): T {
    contract {
        callsInPlace(default, InvocationKind.AT_MOST_ONCE)
    }
    return (this as? UiData.Success)?.data ?: default()
}