package dreammaker.android.expensetracker.ui.account.inputaccount

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.ui.component.SaveCancelActionButtons
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AccountInputScreen(
    isEdit: Boolean,
    viewModel: AccountInputViewModel,
    exit: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // handle saving events
    LaunchedEffect(uiState.savingSuccess) {
        if (uiState.savingSuccess) {
            if (isEdit) {
                exit()
            } else {
                viewModel.onShowAddMoreDialog(true)
            }
        }
    }

    // handle errors
    LaunchedEffect(uiState.savingError, uiState.loadingError) {
        uiState.savingError?.let {
            QuickMessages.toastError(
                context,
                context.getString(R.string.account_save_unsuccessful),
                true
            )
        }
        uiState.loadingError?.let {
            QuickMessages.toastError(
                context,
                context.getString(R.string.message_account_not_found),
                true
            )
            exit()
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
                viewModel.saveAccount()
            },
            name = uiState.name,
            onNameChange = {
                viewModel.onNameChange(it)
            },
            balance = uiState.balance,
            onBalanceChange = {
                viewModel.onBalanceChange(it)
            },
            nameError = uiState.nameError?.let { stringResource(it) },
            balanceError = uiState.balanceError?.let { stringResource(it) }
        )
    }

    if (uiState.showAddMoreDialog) {
        YesNoDialog(
            header = stringResource(R.string.add_more_account),
            body = "",
            yesText = stringResource(R.string.label_yes),
            noText = stringResource(R.string.label_no),
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
            .verticalScroll(rememberScrollState())
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

        SaveCancelActionButtons(
            isSaving = isSaving,
            onCancel = onCancel,
            onSave = onSave
        )
    }
}

//--- Preview -----------------


@PreviewLightDark
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
