package dreammaker.android.expensetracker.ui.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import kotlinx.coroutines.CoroutineScope

data class SnackBarVisualsImpl(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
): SnackbarVisuals

typealias SnackBarCallback = (visuals: SnackbarVisuals, scope: CoroutineScope?, onResult: ((SnackbarResult)-> Unit)?)-> Unit