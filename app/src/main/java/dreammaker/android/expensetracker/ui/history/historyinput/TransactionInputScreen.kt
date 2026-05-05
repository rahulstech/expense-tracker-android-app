package dreammaker.android.expensetracker.ui.history.historyinput

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.colorResource
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
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import java.time.LocalDate


@Composable
fun TransactionInputScreen(
    viewModel: HistoryInputViewModel,
    isEdit: Boolean,
    onAddNewAccount: () -> Unit,
    onAddNewGroup: () -> Unit,
    exit: ()-> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Handle saving events
    LaunchedEffect(uiState.savingSuccess) {
        if (uiState.savingSuccess) {
            if (isEdit) {
                QuickMessages.toastSuccess(context, context.getString(R.string.message_success_save_history))
                exit()
            } else {
                viewModel.onShowAddMoreDialog(true)
            }
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

    if (uiState.showAddMoreDialog) {
        YesNoDialog(
            header = stringResource(R.string.message_success_save_history_ask_add_more),
            onYes = {
                viewModel.resetInput()
            },
            onNo = {
                viewModel.onShowAddMoreDialog(false)
                exit()
            },
            dismissOnBackPressed = false,
            dismissOnClickOutside = false
        )
    }

    if (uiState.isLoadingHistory) {
        FullScreenLoading()
    } else {
        TransactionInputForm(
            uiState = uiState,
            onDateChange = { viewModel.onDateChange(it) },
            onAccountSelected = { viewModel.onAccountSelected(it) },
            onAddNewAccount = onAddNewAccount,
            onGroupSelected = { viewModel.onGroupSelected(it) },
            onAddNewGroup = onAddNewGroup,
            onAmountChange = { viewModel.onAmountChange(it) },
            onTypeChange = { viewModel.onTypeChange(it) },
            onNotesChange = { viewModel.onNoteChange(it) },
        )
    }
}


@Composable
fun TransactionInputForm(
    uiState: HistoryInputUIState,
    onDateChange: (LocalDate)-> Unit,
    onAccountSelected: (Account?)-> Unit,
    onAddNewAccount: () -> Unit,
    onGroupSelected: (Group?)-> Unit,
    onAddNewGroup: () -> Unit,
    onAmountChange: (String) -> Unit,
    onTypeChange: (Boolean) -> Unit,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: explain why LazyColumn over Column
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
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
            TransactionAmountSection(
                amount = uiState.amount,
                onAmountChange = onAmountChange,
                isCredit = uiState.isCredit,
                onTypeChange = onTypeChange,
                error = uiState.amountError?.let{ stringResource(it) }
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
                data = SelectionDropdownData(
                    options = uiState.accounts,
                    quickOptions = uiState.recentAccounts
                ),
                selectedAccount = uiState.account,
                onAccountSelected = onAccountSelected,
                onAddNewAccount = onAddNewAccount,
                error = uiState.accountError?.let { stringResource(it) }
            )
        }

        item {
            GroupSelectionSection(
                data = SelectionDropdownData(
                    options = uiState.groups,
                    quickOptions = uiState.recentGroups
                ),
                selectedGroup = uiState.group,
                onGroupSelected = onGroupSelected,
                onAddNewGroup = onAddNewGroup
            )
        }
    }
}

@Composable
private fun TransactionAmountSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    isCredit: Boolean,
    onTypeChange: (Boolean) -> Unit,
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
                    color = if (isCredit) ExpenseTrackerTheme.appColor.credit else ExpenseTrackerTheme.appColor.debit,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TransactionTypeChip(
                    text = stringResource(R.string.credit),
                    selected = isCredit,
                    onClick = { onTypeChange(true) },
                    selectedContainerColor = colorResource(dreammaker.android.expensetracker.core.R.color.colorCredit),
                    selectedContentColor = colorResource(dreammaker.android.expensetracker.core.R.color.colorOnCredit)
                )
                TransactionTypeChip(
                    text = stringResource(R.string.debit),
                    selected = !isCredit,
                    onClick = { onTypeChange(false) },
                    selectedContainerColor = colorResource(dreammaker.android.expensetracker.core.R.color.colorDebit),
                    selectedContentColor = colorResource(dreammaker.android.expensetracker.core.R.color.colorOnDebit)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedContainerColor: Color,
    selectedContentColor: Color,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        shape = RoundedCornerShape(24.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = selectedContainerColor,
            selectedLabelColor = selectedContentColor,
            selectedLeadingIconColor = selectedContentColor,
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = if (!selected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
        else null
    )
}

@Composable
private fun AccountSelectionSection(
    data: SelectionDropdownData<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account?) -> Unit,
    onAddNewAccount: () -> Unit,
    error: String? = null,
) {
    SelectionDropdown(
        label = stringResource(R.string.account),
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
private fun GroupSelectionSection(
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
        addNewOptionContent = {
            Text(
                text = stringResource(R.string.add_new_group),
                style = MaterialTheme.typography.labelMedium
            )
        },
        onAddNewOption = onAddNewGroup
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
                text = stringResource(R.string.loading_history),
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


//---------------------------
//  Preview
//---------------------------

@PreviewLightDark
@Composable
fun TransactionInputScreenPreview() {
    val accounts = listOf(
        Account(name = "Main Checking (***4920)", id = 1),
        Account(name = "Savings", id = 2),
        Account(name = "Credit Card", id = 3)
    )
    val groups = listOf(
        Group(name = "Shopping", id = 1),
        Group(name = "Groceries", id = 2),
        Group(name = "Dining", id = 3)
    )

    var uiState by remember {
        mutableStateOf(
            HistoryInputUIState(
                date = LocalDate.now(),
                group = groups[0],
                account = accounts[0],
                amount = "125.50",
                isCredit = false,
                note = "Grocery shopping at local market",
                accounts = accounts,
                recentAccounts = listOf(accounts[0], accounts[2]),
                groups = groups,
                recentGroups = listOf(groups[1], groups[2]),
            )
        )
    }

    ExpenseTrackerTheme {
        TransactionInputForm(
            uiState = uiState,
            onDateChange = { uiState = uiState.copy(date = it) },
            onAccountSelected = { uiState = uiState.copy(account = it) },
            onAddNewAccount = {},
            onGroupSelected = { uiState = uiState.copy(group = it) },
            onAddNewGroup = {},
            onAmountChange = { uiState = uiState.copy(amount = it) },
            onTypeChange = { uiState = uiState.copy(isCredit = it) },
            onNotesChange = { uiState = uiState.copy(note = it) }
        )
    }
}

@PreviewLightDark
@Composable
fun TransactionInputScreenLoadingPreview() {
    ExpenseTrackerTheme {
        FullScreenLoading()
    }
}

@PreviewLightDark
@Composable
fun TransactionInputScreenErrorPreview() {
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
        accounts = accounts
    )
    ExpenseTrackerTheme {
        TransactionInputForm(
            uiState = uiState,
            onDateChange = {},
            onAccountSelected = {},
            onAddNewAccount = {},
            onGroupSelected = {},
            onAddNewGroup = {},
            onAmountChange = {},
            onTypeChange = {},
            onNotesChange = {}
        )
    }
}
