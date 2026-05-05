package dreammaker.android.expensetracker.ui.history.historyinput

import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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


@Immutable
data class SelectionDropdownData<T>(
    val options: List<T>,
    val quickOptions: List<T>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionDropdown(
    label: String,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    data: SelectionDropdownData<T>,
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

                // NOTE: modifier apply left to right. therefore if fillMaxWidth apply first then weight will be ignored.
                // in the following case since weight is used fillMaxWidth is totally unnecessary
                Box(
                    modifier = Modifier
                        //.fillMaxWidth()
                        .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 6.dp)
                        .weight(1f)
                ) {
                    if (selectedOption != null) {
                        // InputChip is the right chose when chip has other clickable child.
                        // in the following case it has trailing close button.
                        InputChip(
                            selected = true,
                            onClick = { },
                            label = {
                                Text(
                                    text = labelProvider(selectedOption),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = {
                                // using IconButton over Icon with clickable Modifier is more appropriate
                                // since chips are clickable therefore IconButton properly differentiate
                                // the click event on chip or trailingIcon
                                IconButton(
                                    onClick = { onOptionSelected(null) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            },
                            shape = MaterialTheme.shapes.large,
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                            border = null,
                            modifier = Modifier.widthIn(max = 200.dp),
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

            if (expanded) {
                ExposedDropdownMenu(
                    expanded = true,
                    onDismissRequest = { expanded = false }
                ) {
                    data.options.forEach { option ->
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
            data.quickOptions.forEach { option ->
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
                data = SelectionDropdownData(
                    options = listOf("Food", "Transport", "Shopping", "Entertainment"),
                    quickOptions = listOf("Food", "Transport"),
                ),
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
                data = SelectionDropdownData(
                    options = listOf("State Bank Of India", "Cash", "Credit Card"),
                    quickOptions = listOf("State Bank Of India", "Cash"),
                ),
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
                data = SelectionDropdownData(
                    options = listOf("Bank", "Cash", "Credit Card"),
                    quickOptions = listOf("Bank", "Cash"),
                ),
                labelProvider = { it },
                error = "Please select an account"
            )
        }
    }
}
