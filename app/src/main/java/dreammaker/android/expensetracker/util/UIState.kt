package dreammaker.android.expensetracker.util

sealed class UIState {
    data class UILoading(val data: Any? = null): UIState()
    
    data class UISuccess(val data: Any? = null): UIState() {

        @Suppress("UNCHECKED_CAST")
        fun <T> asData(): T? = data as T?
    }
    
    data class UIError(val cause: Throwable? = null, val data: Any? = null): UIState()
}