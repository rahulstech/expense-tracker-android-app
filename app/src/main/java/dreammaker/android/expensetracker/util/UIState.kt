package dreammaker.android.expensetracker.util

sealed class UIState<out T> {
    data class UILoading(val data: Any? = null): UIState<Nothing>()
    
    data class UISuccess<out T>(val data: T? = null): UIState<T>()
    
    data class UIError(val cause: Throwable? = null, val data: Any? = null): UIState<Nothing>()
}