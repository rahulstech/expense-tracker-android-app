package dreammaker.android.expensetracker.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember

private val DEBUG = true

@Composable
fun DebugRecompose(tag: String) {
    if (DEBUG) {
        val count = remember { mutableIntStateOf(0) }

        SideEffect {
            count.intValue++
            android.util.Log.d("RECOMPOSE", "$tag -> ${count.intValue}")
        }
    }
}