package dreammaker.android.expensetracker.ui.history.historyinput

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dreammaker.android.expensetracker.R

@Composable
fun NotesSection(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.note_optional),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            modifier = Modifier.Companion.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            trailingIcon = {
                if (notes.isNotEmpty()) {
                    IconButton(onClick = { onNotesChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )
    }
}