package dreammaker.android.expensetracker.core.ui

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun secondaryButtonColors(): ButtonColors {
    val color = MaterialTheme.colorScheme.secondary
    return ButtonDefaults.filledTonalButtonColors(
        containerColor = color.copy(alpha = 0.24f),
        contentColor = color,
        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    )
}
