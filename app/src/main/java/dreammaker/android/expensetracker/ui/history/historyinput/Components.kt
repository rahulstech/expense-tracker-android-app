package dreammaker.android.expensetracker.ui.history.historyinput

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.ui.component.AmountTextField
import dreammaker.android.expensetracker.ui.component.AmountTextFiledColors
import dreammaker.android.expensetracker.ui.component.MessageDialog
import dreammaker.android.expensetracker.ui.component.ProgressDialog
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group

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
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            trailingIcon = {
                if (notes.isNotEmpty()) {
                    IconButton(onClick = { onNotesChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "clear note",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun AmountSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    amountError: String? = null,
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
    typeChooser: (@Composable ()-> Unit)? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AmountTextField(
                value = amount,
                onValueChange = onAmountChange,
                labelText = stringResource(R.string.amount),
                errorText = amountError,
                colors = AmountTextFiledColors.defaultColors(
                    valueColor = amountColor,
                    borderFocusedColor = Color.Transparent,
                    borderUnfocusedColor = Color.Transparent,
                    borderErrorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            typeChooser?.invoke()
        }
    }
}


@Composable
fun AccountSelectionSection(
    labelText: String,
    data: SelectionDropdownData<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account?) -> Unit,
    onAddNewAccount: () -> Unit,
    errorText: String? = null,
) {
    SelectionDropdown(
        label = labelText,
        selectedOption = selectedAccount,
        onOptionSelected = onAccountSelected,
        data = data,
        labelProvider = { it.name },
        error = errorText,
        addNewOptionText = stringResource(R.string.label_add_account),
        onAddNewOption = onAddNewAccount
    )
}

@Composable
fun GroupSelectionSection(
    data: SelectionDropdownData<Group>,
    selectedGroup: Group?,
    onGroupSelected: (Group?) -> Unit,
    onAddNewGroup: () -> Unit
) {
    SelectionDropdown(
        label = stringResource(R.string.group_optional),
        selectedOption = selectedGroup,
        onOptionSelected = onGroupSelected,
        data = data,
        labelProvider = { it.name },
        addNewOptionText = stringResource(R.string.label_add_group),
        onAddNewOption = onAddNewGroup
    )
}


@Composable
fun HistoryInputDialogComponent(
    dialog: HistoryInputDialog,
    onDismiss: (exit: Boolean)-> Unit,
    onAddMore: ()-> Unit,
) {
    when(dialog) {

        is HistoryInputDialog.SaveError -> {
            MessageDialog(
                body = stringResource(R.string.message_error_save_history),
                onDismiss = { onDismiss(false) }
            )
        }

        is HistoryInputDialog.AddMore -> {
            YesNoDialog(
                header = stringResource(R.string.label_add_more),
                onYes = onAddMore,
                onNo = { onDismiss(true) },
            )
        }

        is HistoryInputDialog.PendingSave -> {
            ProgressDialog(
                stringResource(R.string.message_saving_progress),
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        }

        else -> {}
    }
}