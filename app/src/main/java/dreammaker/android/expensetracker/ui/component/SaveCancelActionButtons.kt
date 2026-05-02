package dreammaker.android.expensetracker.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dreammaker.android.expensetracker.R

@Composable
fun SaveCancelActionButtons(
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            enabled = !isSaving
        ) {
            Text(
                text = stringResource(R.string.cancel),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
        ButtonWithProgressBar(
            buttonText = {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            showProgressBar = isSaving,
            shape = MaterialTheme.shapes.medium,
        )
    }
}
