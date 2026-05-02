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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
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
import dreammaker.android.expensetracker.ui.component.SaveCancelActionButtons
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val accounts by viewModel.allAccounts.collectAsState(emptyList())
    val recentAccounts by viewModel.lastUsedAccounts.collectAsState(emptyList())
    val groups by viewModel.allGroups.collectAsState(emptyList())
    val recentGroups by viewModel.lastUsedGroups.collectAsState(emptyList())

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
            selectedDate = uiState.date,
            onDateChange = { viewModel.onDateChange(it) },
            accounts = accounts,
            recentAccounts = recentAccounts,
            selectedAccount = uiState.account,
            onAccountSelected = { viewModel.onAccountSelected(it) },
            onAddNewAccount = onAddNewAccount,
            groups = groups,
            recentGroups = recentGroups,
            selectedGroup = uiState.group,
            onGroupSelected = { viewModel.onGroupSelected(it) },
            onAddNewGroup = onAddNewGroup,
            amount = uiState.amount,
            onAmountChange = { viewModel.onAmountChange(it) },
            isCredit = uiState.isCredit,
            onTypeChange = { viewModel.onTypeChange(it) },
            notes = uiState.note,
            onNotesChange = { viewModel.onNoteChange(it) },
            isSaving = uiState.isSaving,
            amountError = uiState.amountError?.let { stringResource(it) },
            accountError = uiState.accountError?.let { stringResource(it) },
            onCancel = exit,
            onSave = {
                viewModel.saveHistory()
            }
        )
    }
}


@Composable
fun TransactionInputForm(
    selectedDate: LocalDate,
    onDateChange: (LocalDate)-> Unit,
    accounts: List<Account>,
    groups: List<Group>,
    recentAccounts: List<Account>,
    recentGroups: List<Group>,
    selectedAccount: Account?,
    onAccountSelected: (Account?)-> Unit,
    onAddNewAccount: () -> Unit,
    selectedGroup: Group?,
    onGroupSelected: (Group?)-> Unit,
    onAddNewGroup: () -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    isCredit: Boolean,
    onTypeChange: (Boolean) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    amountError: String? = null,
    accountError: String? = null,
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

        TransactionAmountSection(
            amount = amount,
            onAmountChange = onAmountChange,
            isCredit = isCredit,
            onTypeChange = onTypeChange,
            error = amountError
        )

        AccountSelectionSection(
            selectedAccount = selectedAccount,
            onAccountSelected = onAccountSelected,
            accounts = accounts,
            recentAccounts = recentAccounts,
            onAddNewAccount = onAddNewAccount,
            error = accountError
        )

        GroupSelectionSection(
            selectedGroup = selectedGroup,
            onGroupSelected = onGroupSelected,
            groups = groups,
            recentGroups = recentGroups,
            onAddNewGroup = onAddNewGroup
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
                    color = if (isCredit) Color(0xFF4B7831) else Color(0xFFCC2929),
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
private fun GroupSelectionSection(
    selectedGroup: Group?,
    onGroupSelected: (Group?) -> Unit,
    groups: List<Group>,
    recentGroups: List<Group>,
    onAddNewGroup: () -> Unit
) {
    SelectionDropdown(
        label = stringResource(R.string.group_optional),
        selectedOption = selectedGroup,
        onOptionSelected = onGroupSelected,
        options = groups,
        recentOptions = recentGroups,
        labelProvider = { it.name },
        addNewOptionContent = {
            Text(
                text = stringResource(R.string.label_add_group),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
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
    val recentAccounts = listOf(accounts[0], accounts[2])
    val groups = listOf(
        Group(name = "Shopping", id = 1),
        Group(name = "Groceries", id = 2),
        Group(name = "Dining", id = 3)
    )
    val recentGroups = listOf(groups[1],groups[2])
    var selectedAccount by remember { mutableStateOf<Account?>(accounts[0]) }
    var selectedGroup by remember { mutableStateOf<Group?>(groups[0]) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var amount by remember { mutableStateOf("") }
    var isCredit by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    ExpenseTrackerTheme {
        TransactionInputForm(
            selectedDate = selectedDate,
            onDateChange = { selectedDate = it },
            accounts = accounts,
            recentAccounts = recentAccounts,
            selectedAccount = selectedAccount,
            onAccountSelected = { selectedAccount = it },
            onAddNewAccount = {},
            groups = groups,
            recentGroups = recentGroups,
            selectedGroup = selectedGroup,
            onGroupSelected = { selectedGroup = it },
            onAddNewGroup = {},
            amount = amount,
            onAmountChange = { amount = it },
            isCredit = isCredit,
            onTypeChange = { isCredit = it },
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
