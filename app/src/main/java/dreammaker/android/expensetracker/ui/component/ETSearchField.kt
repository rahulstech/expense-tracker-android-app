package dreammaker.android.expensetracker.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme

@Composable
fun ETSearchField(
    value: String,
    onValueChange: (String)-> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "",
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(.8f),
            shape = MaterialTheme.shapes.extraLarge,
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholderText,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            },
            trailingIcon = {
                if (value.isNotBlank()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null) // TODO: add content description for clear button
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

@PreviewLightDark
@Composable
fun ETSearchFieldPreview() {

    var query by remember { mutableStateOf("") }

    ExpenseTrackerTheme {
        Scaffold { contentPadding ->
            ETSearchField(
                value = query,
                onValueChange = { query = it },
                placeholderText = "Search Something",
                modifier = Modifier.fillMaxWidth().padding(contentPadding)
            )
        }
    }
}