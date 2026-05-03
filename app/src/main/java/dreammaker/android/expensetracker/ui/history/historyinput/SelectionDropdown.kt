package dreammaker.android.expensetracker.ui.history.historyinput

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionDropdown(
    label: String,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    options: List<T>,
    recentOptions: List<T>,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier,
    error: String? = null,
    addNewOptionContent: (@Composable () -> Unit)? = null,
    onAddNewOption: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .border(
                        color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                        width = if (error != null) 2.dp else 1.dp,
                        shape = MaterialTheme.shapes.medium
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 6.dp)
                        .weight(1f)
                ) {
                    if (selectedOption != null) {
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = labelProvider(selectedOption),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { onOptionSelected(null) },
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                            border = null,
                            modifier = Modifier.widthIn(max = 200.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 6.dp, top = 8.dp, bottom = 8.dp, end = 12.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(labelProvider(option)) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        // Quick Links
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            recentOptions.forEach { option ->
                SuggestionChip(
                    onClick = { onOptionSelected(option) },
                    label = {
                        Text(
                            text = labelProvider(option),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = null,
                    modifier = Modifier.widthIn(max = 120.dp)
                )
            }

            addNewOptionContent?.let { content ->
                SuggestionChip(
                    onClick = { onAddNewOption?.invoke() },
                    label = { content() },
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = null
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectionDropdownPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            SelectionDropdown(
                label = "Category",
                selectedOption = "Entertainments & Travelling",
                onOptionSelected = {},
                options = listOf("Food", "Transport", "Shopping", "Entertainment"),
                recentOptions = listOf("Food", "Transport"),
                labelProvider = { it }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectionDropdownNoSelectionPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            SelectionDropdown(
                label = "Account",
                selectedOption = null,
                onOptionSelected = {},
                options = listOf("State Bank Of India", "Cash", "Credit Card"),
                recentOptions = listOf("State Bank Of India", "Cash"),
                labelProvider = { it },
                addNewOptionContent = { Text("+ Add New Account") }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectionDropdownErrorPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            SelectionDropdown(
                label = "Account",
                selectedOption = null,
                onOptionSelected = {},
                options = listOf("Bank", "Cash", "Credit Card"),
                recentOptions = listOf("Bank", "Cash"),
                labelProvider = { it },
                error = "Please select an account"
            )
        }
    }
}
