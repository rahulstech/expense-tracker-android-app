package dreammaker.android.expensetracker.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> Flow<T>.onFirst(action: suspend (T)-> Unit): Flow<T> = flow {
    var consumed = false
    collect { value ->
        if (!consumed) {
            action(value)
            consumed = true
        }
        emit(value)
    }
}