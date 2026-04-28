package dreammaker.android.expensetracker.ui.account.inputaccount

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import kotlinx.coroutines.flow.collectLatest
import rahulstech.android.expensetracker.domain.model.Account

@Composable
fun AccountInputScreen(
    isEdit: Boolean,
    viewModel: AccountInputViewModel,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    var showAddMoreDialog by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var balance by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var balanceError by rememberSaveable { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()


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


    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when(event) {
                is AccountInputUIEvent.SaveError -> {

                }
                is AccountInputUIEvent.SaveSuccessful -> {
                    showAddMoreDialog = true
                }
            }
        }
    }

    LaunchedEffect(uiState.account) {

        if (isEdit) {
            if (!uiState.isLoadingAccount) {
                uiState.account?.let {
                    name = it.name
                    balance = it.balance.toString()
                }
                    ?: onExit()
            }
        }
    }

    AccountInputForm(
        isSaving = uiState.isSaving,
        onCancel = onExit,
        onSave = {
            if (validateName() && validateBalance()) {
                viewModel.saveAccount(it, isEdit)
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
                onExit()
            },
            dismissOnBackPressed = false,
            dismissOnClickOutside = false
        )
    }
}




@Composable
fun AccountInputForm(
    name: String,
    onNameChange: (String)-> Unit,
    balance: String,
    onBalanceChange: (String)-> Unit,
    nameError: String?,
    balanceError: String?,
    onSave: (Account) -> Unit,
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

        Button(
            onClick = {
                val amount = balance.toFloatOrNull() ?: 0f
                val account = Account(
                    id = 0L,
                    name = name,
                    balance = amount
                )
                onSave(account)
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save))
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.cancel))
        }
    }
}










@Preview(showBackground = true)
@Composable
fun AccountInputFormPreview() {
    ExpenseTrackerTheme {
        AccountInputForm(
            name = "",
            onNameChange = {},
            balance = "",
            onBalanceChange = {},
            nameError = null,
            balanceError = null,
            onSave = {},
            onCancel = {},
            isSaving = false
        )
    }
}