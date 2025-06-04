package dreammaker.android.expensetracker.util

sealed class UIState<out T> {
    data class UILoading(val message: CharSequence? = null, val progressCurrent: Int = -1, val progressMax: Int = -1): UIState<Nothing>()
    
    data class UIData<T>(val data: T): UIState<T>()
    
    data class UIError(val cause: Throwable, val message: CharSequence? = null): UIState<Nothing>()
}