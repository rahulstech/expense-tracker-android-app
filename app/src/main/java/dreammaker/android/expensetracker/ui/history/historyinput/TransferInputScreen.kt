package dreammaker.android.expensetracker.ui.history.historyinput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.ui.component.DatePicker
import rahulstech.android.expensetracker.domain.model.Account
import java.time.LocalDate

@Composable
fun TransferInputScreen(
    viewModel: HistoryInputViewModel,
    isEdit: Boolean,
    onAddNewAccount: () -> Unit,
    exit: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
//    val accounts by viewModel.allAccounts.collectAsState(emptyList())
//    val recentAccounts by viewModel.lastUsedAccounts.collectAsState(emptyList())

    // Handle saving events
    LaunchedEffect(uiState.savingSuccess) {
        if (uiState.savingSuccess) {
            QuickMessages.toastSuccess(context, context.getString(R.string.message_success_save_history))
            exit()
        }
    }

    // Handle errors
    LaunchedEffect(uiState.savingError, uiState.historyLoadingError) {
        uiState.savingError?.let {
            QuickMessages.toastError(context, context.getString(R.string.message_error_save_history))
        }
        uiState.historyLoadingError?.let {
            QuickMessages.toastError(context, context.getString(R.string.message_history_not_found))
        }
    }

    if (uiState.isLoadingHistory) {
        FullScreenLoading()
    } else {
        TransferInputForm(
            uiState = uiState,
            onDateChange = { viewModel.onDateChange(it) },
            onSourceAccountSelected = { viewModel.onAccountSelected(it) },
            onDestinationAccountSelected = { viewModel.onDestinationAccountSelected(it) },
            onAddNewAccount = onAddNewAccount,
            onAmountChange = { viewModel.onAmountChange(it) },
            onNotesChange = { viewModel.onNoteChange(it) },
        )
    }
}

@Composable
fun TransferInputForm(
    uiState: HistoryInputUIState,
    onDateChange: (LocalDate) -> Unit,
    onSourceAccountSelected: (Account?) -> Unit,
    onDestinationAccountSelected: (Account?) -> Unit,
    onAddNewAccount: () -> Unit,
    onAmountChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn (
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            DatePicker(
                selectedDate = uiState.date,
                onDateSelected = onDateChange
            )
        }

        item {
            TransferAmountSection(
                amount = uiState.amount,
                onAmountChange = onAmountChange,
                error = uiState.amountError?.let { stringResource(it) }
            )
        }

        item {
            NotesSection(
                notes = uiState.note,
                onNotesChange = onNotesChange
            )
        }

        item {
            AccountSelectionSection(
                selectedAccount = uiState.account,
                onAccountSelected = onSourceAccountSelected,
                data = SelectionDropdownData(
                    options = uiState.accounts,
                    quickOptions = uiState.recentAccounts,
                ),
                onAddNewAccount = onAddNewAccount,
                error = uiState.accountError?.let { stringResource(it) },
                label = stringResource(R.string.label_history_input_source_account)
            )
        }

        item {
            AccountSelectionSection(
                selectedAccount = uiState.destinationAccount,
                onAccountSelected = onDestinationAccountSelected,
                data = SelectionDropdownData(
                    options = uiState.accounts,
                    quickOptions = uiState.recentAccounts
                ),
                onAddNewAccount = onAddNewAccount,
                error = uiState.destinationAccountError?.let{ stringResource(it) },
                label = stringResource(R.string.label_history_input_destination_account)
            )
        }
    }
}

@Composable
fun TransferAmountSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    error: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.amount),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "0.00",
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                textStyle = MaterialTheme.typography.displayMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                ),
                prefix = {
                    Text(
                        text = "$",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                isError = error != null,
                supportingText = error?.let {
                    {
                        Text(
                            text = it,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true
            )
        }
    }
}

@Composable
private fun AccountSelectionSection(
    label: String,
    selectedAccount: Account?,
    onAccountSelected: (Account?) -> Unit,
    data: SelectionDropdownData<Account>,
    onAddNewAccount: () -> Unit,
    error: String? = null,
) {
    SelectionDropdown(
        label = label,
        selectedOption = selectedAccount,
        onOptionSelected = onAccountSelected,
        data = data,
        labelProvider = { it.name },
        error = error,
        addNewOptionContent = {
            Text(
                text = stringResource(R.string.add_new_account),
                style = MaterialTheme.typography.labelMedium,
            )
        },
        onAddNewOption = onAddNewAccount
    )
}

@Composable
private fun FullScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(.85f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.loading_transfer),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}


//-----------------------------------------
//      Preview
//-----------------------------------------

@PreviewLightDark
@Composable
fun TransferInputScreenPreview() {
    val accounts = listOf(
        Account(name = "Main Checking (***4920)", id = 1),
        Account(name = "Savings", id = 2),
        Account(name = "Credit Card", id = 3)
    )

    var uiState by remember {
        mutableStateOf(
            HistoryInputUIState(
                date = LocalDate.now(),
                account = accounts[0],
                destinationAccount = accounts[1],
                amount = "100.00",
                note = "Transfer to savings",
                accounts = accounts,
                recentAccounts = listOf(accounts[0], accounts[2]),
            )
        )
    }

    ExpenseTrackerTheme {
        TransferInputForm(
            uiState = uiState,
            onDateChange = { uiState = uiState.copy(date = it) },
            onSourceAccountSelected = { uiState = uiState.copy(account = it) },
            onDestinationAccountSelected = { uiState = uiState.copy(destinationAccount = it) },
            onAddNewAccount = {},
            onAmountChange = { uiState = uiState.copy(amount = it) },
            onNotesChange = { uiState = uiState.copy(note = it) }
        )
    }
}

@PreviewLightDark
@Composable
fun TransferInputScreenLoadingPreview() {
    ExpenseTrackerTheme {
        FullScreenLoading()
    }
}

@PreviewLightDark
@Composable
fun TransferInputScreenErrorPreview() {
    val accounts = listOf(
        Account(name = "Main Checking (***4920)", id = 1),
        Account(name = "Savings", id = 2),
        Account(name = "Credit Card", id = 3)
    )
    val uiState = HistoryInputUIState(
        amount = "abc",
        amountError = R.string.error_invalid_amount,
        account = null,
        accountError = R.string.error_no_selection,
        destinationAccount = null,
        destinationAccountError = R.string.error_no_selection,
        accounts = accounts
    )
    ExpenseTrackerTheme {
        TransferInputForm(
            uiState = uiState,
            onDateChange = {},
            onSourceAccountSelected = {},
            onDestinationAccountSelected = {},
            onAddNewAccount = {},
            onAmountChange = {},
            onNotesChange = {}
        )
    }
}
