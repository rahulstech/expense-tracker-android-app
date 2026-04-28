package dreammaker.android.expensetracker.ui.account.inputaccount

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.ui.component.ButtonWithProgressBar
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Account

@Composable
fun AccountInputScreen(
    isEdit: Boolean,
    viewModel: AccountInputViewModel,
    exit: () -> Unit,
) {
    val context = LocalContext.current
    var showAddMoreDialog by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var balance by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var balanceError by rememberSaveable { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    // validation methods
    fun validateName(): Boolean {
        nameError = when {
            name.isBlank() -> context.getString(R.string.error_empty_account_name_input)
            else -> null
        }
        return null == nameError
    }

    fun validateBalance(): Boolean {
        val amount = balance.toFloatOrNull()
        balanceError = when {
            balance.isBlank() -> context.getString(R.string.error_empty_balance_input)
            amount == null -> context.getString(R.string.error_invalid_balance_input)
            else -> null
        }
        return null == balanceError
    }

    // handle ui events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when(event) {
                is AccountInputUIEvent.SaveError -> {
                    QuickMessages.toastError(
                        context,
                        context.getString(R.string.account_save_unsuccessful),
                        true
                    )
                }
                is AccountInputUIEvent.SaveSuccessful -> {
                    showAddMoreDialog = true
                }
                is AccountInputUIEvent.LoadingError -> {
                    QuickMessages.toastError(
                        context,
                        context.getString(R.string.message_account_not_found),
                        true
                    )
                    exit()
                }
            }
        }
    }

    // handle account loading
    LaunchedEffect(uiState.account?.id) {
        if (isEdit) {
            if (!uiState.isLoadingAccount) {
                // on loading complete if account found set
                // the initial name and balance to the loaded account name and balance
                // if not found show toast and exit
                uiState.account?.let {
                    name = it.name
                    balance = it.balance.toString()
                }
            }
        }
    }

    if (uiState.isLoadingAccount) {
        FullScreenLoading()
    }
    else {
        AccountInputForm(
            isSaving = uiState.isSaving,
            onCancel = exit,
            onSave = {
                if (validateName() && validateBalance()) {
                    val amount = balance.toFloat()
                    val account = if (isEdit) {
                        Account(
                            id = uiState.account?.id ?: 0,
                            name = name,
                            balance = amount
                        )
                    } else {
                        Account(
                            name = name,
                            balance = amount
                        )
                    }
                    viewModel.saveAccount(account, isEdit)
                }
            },
            name = name,
            onNameChange = {
                name = it
                nameError = null
            },
            balance = balance,
            onBalanceChange = {
                balance = it
                balanceError = null
            },
            nameError = nameError,
            balanceError = balanceError
        )
    }

    if (showAddMoreDialog) {
        YesNoDialog(
            header = stringResource(R.string.add_more_account),
            body = "",
            yesText = stringResource(R.string.label_yes),
            noText = stringResource(R.string.label_no),
            onYes = {
                showAddMoreDialog = false
                name = ""
                balance = ""
            },
            onNo = {
                showAddMoreDialog = false
                exit()
            },
            dismissOnBackPressed = false,
            dismissOnClickOutside = false
        )
    }
}

//-------- Full Screen Account Loading ---------
@Composable
fun FullScreenLoading() {

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(.85f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.loading_account),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

//-------- Account Input Form -----------

@Composable
fun AccountInputForm(
    name: String,
    onNameChange: (String)-> Unit,
    balance: String,
    onBalanceChange: (String)-> Unit,
    nameError: String?,
    balanceError: String?,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isSaving: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.account_name)) },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } },
            trailingIcon = {
                if (name.isNotEmpty()) {
                    IconButton(onClick = { onNameChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = balance,
            onValueChange = onBalanceChange,
            label = { Text(stringResource(R.string.balance)) },
            modifier = Modifier.fillMaxWidth(),
            isError = balanceError != null,
            supportingText = balanceError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        ButtonWithProgressBar(
            buttonText = stringResource(R.string.save),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            progressText = stringResource(R.string.saving),
            showProgressBar = isSaving
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text(stringResource(R.string.cancel))
        }
    }
}

//--- Preview -----------------


@Preview(showBackground = true)
@Composable
fun FullScreenLoadingPreview() {
    ExpenseTrackerTheme {
        FullScreenLoading()
    }
}

@PreviewScreenSizes
@Composable
fun AccountInputFormPreview() {
    var isSaving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    ExpenseTrackerTheme {
        AccountInputForm(
            name = "",
            onNameChange = {},
            balance = "",
            onBalanceChange = {},
            nameError = null,
            balanceError = null,
            onSave = {
                coroutineScope.launch {
                    isSaving = true
                    delay(3000)
                    isSaving = false
                }
            },
            onCancel = {},
            isSaving = isSaving
        )
    }
}