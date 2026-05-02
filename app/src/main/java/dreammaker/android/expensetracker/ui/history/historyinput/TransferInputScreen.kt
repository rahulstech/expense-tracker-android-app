package dreammaker.android.expensetracker.ui.history.historyinput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
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
import dreammaker.android.expensetracker.ui.component.SaveCancelActionButtons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val accounts by viewModel.allAccounts.collectAsState(emptyList())
    val recentAccounts by viewModel.lastUsedAccounts.collectAsState(emptyList())

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
            selectedDate = uiState.date,
            onDateChange = { viewModel.onDateChange(it) },
            accounts = accounts,
            recentAccounts = recentAccounts,
            sourceAccount = uiState.account,
            onSourceAccountSelected = { viewModel.onAccountSelected(it) },
            destinationAccount = uiState.destinationAccount,
            onDestinationAccountSelected = { viewModel.onDestinationAccountSelected(it) },
            onAddNewAccount = onAddNewAccount,
            amount = uiState.amount,
            onAmountChange = { viewModel.onAmountChange(it) },
            notes = uiState.note,
            onNotesChange = { viewModel.onNoteChange(it) },
            isSaving = uiState.isSaving,
            amountError = uiState.amountError?.let { stringResource(it) },
            sourceAccountError = uiState.accountError?.let { stringResource(it) },
            destinationAccountError = uiState.destinationAccountError?.let { stringResource(it) },
            onCancel = exit,
            onSave = {
                viewModel.saveHistory()
            }
        )
    }
}

@Composable
fun TransferInputForm(
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    accounts: List<Account>,
    recentAccounts: List<Account>,
    sourceAccount: Account?,
    onSourceAccountSelected: (Account?) -> Unit,
    destinationAccount: Account?,
    onDestinationAccountSelected: (Account?) -> Unit,
    onAddNewAccount: () -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    amountError: String? = null,
    sourceAccountError: String? = null,
    destinationAccountError: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        DatePicker(
            selectedDate = selectedDate,
            onDateSelected = onDateChange
        )

        TransferAmountSection(
            amount = amount,
            onAmountChange = onAmountChange,
            error = amountError
        )

        AccountSelectionSection(
            selectedAccount = sourceAccount,
            onAccountSelected = onSourceAccountSelected,
            accounts = accounts,
            recentAccounts = recentAccounts,
            onAddNewAccount = onAddNewAccount,
            error = sourceAccountError,
            label = stringResource(R.string.label_history_input_source_account)
        )

        AccountSelectionSection(
            selectedAccount = destinationAccount,
            onAccountSelected = onDestinationAccountSelected,
            accounts = accounts,
            recentAccounts = recentAccounts,
            onAddNewAccount = onAddNewAccount,
            error = destinationAccountError,
            label = stringResource(R.string.label_history_input_destination_account)
        )

        NotesSection(
            notes = notes,
            onNotesChange = onNotesChange
        )

        SaveCancelActionButtons(
            onCancel = onCancel,
            onSave = onSave,
            isSaving = isSaving
        )
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
    selectedAccount: Account?,
    onAccountSelected: (Account?) -> Unit,
    accounts: List<Account>,
    recentAccounts: List<Account>,
    onAddNewAccount: () -> Unit,
    error: String? = null,
    label: String = stringResource(R.string.account)
) {
    SelectionDropdown(
        label = label,
        selectedOption = selectedAccount,
        onOptionSelected = onAccountSelected,
        options = accounts,
        recentOptions = recentAccounts,
        labelProvider = { it.name },
        error = error,
        addNewOptionContent = {
            Text(
                text = stringResource(R.string.label_add_account),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
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
    val recentAccounts = listOf(accounts[0], accounts[2])
    var sourceAccount by remember { mutableStateOf<Account?>(accounts[0]) }
    var destinationAccount by remember { mutableStateOf<Account?>(accounts[1]) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    ExpenseTrackerTheme {
        TransferInputForm(
            selectedDate = selectedDate,
            onDateChange = { selectedDate = it },
            accounts = accounts,
            recentAccounts = recentAccounts,
            sourceAccount = sourceAccount,
            onSourceAccountSelected = { sourceAccount = it },
            destinationAccount = destinationAccount,
            onDestinationAccountSelected = { destinationAccount = it },
            onAddNewAccount = {},
            amount = amount,
            onAmountChange = { amount = it },
            notes = notes,
            onNotesChange = { notes = it },
            isSaving = isSaving,
            onSave = {
                coroutineScope.launch {
                    isSaving = true
                    delay(3000)
                    isSaving = false
                }
            },
            onCancel = {}
        )
    }
}
