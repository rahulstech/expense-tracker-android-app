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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.ui.component.NavigationUpIcon
import dreammaker.android.expensetracker.core.ui.component.ScreenScaffold
import dreammaker.android.expensetracker.core.ui.component.TopBarIconAction
import dreammaker.android.expensetracker.core.ui.component.TopBarTitle
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import dreammaker.android.expensetracker.ui.navigation.NavigationEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddAccountRoute(
    onNavigate: (NavigationEvent)-> Unit,
    viewModel: AccountInputViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AccountInputScreen(
        uiState = uiState,
        onNavigate = onNavigate,
        onUiEvent = { viewModel.handleUiEvent(it) }
    )
}

@Composable
fun EditAccountRoute(
    onNavigate: (NavigationEvent)-> Unit,
    viewModel: AccountInputViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AccountInputScreen(
        uiState = uiState,
        onNavigate = onNavigate,
        onUiEvent = { viewModel.handleUiEvent(it) }
    )
}

@Composable
fun AccountInputScreen(
    uiState: AccountInputUiState,
    onNavigate: (NavigationEvent) -> Unit,
    onUiEvent: (AccountInputUiEvent)-> Unit
) {
    val isSaveAndCancelActionEnabled = !uiState.isLoadingAccount && !uiState.isSaving

    ScreenScaffold(
        title = {
            if (uiState.isEditing) {
                TopBarTitle(stringResource(R.string.title_edit_account))
            }
            else {
                TopBarTitle(stringResource(R.string.title_create_account))
            }
        },
        actions = {
            TopBarIconAction(Icons.Default.Done,
                title = stringResource(R.string.save),
                enabled = isSaveAndCancelActionEnabled
            ) {
                onUiEvent(AccountInputUiEvent.SaveAccount)
            }
        },
        navigationIcon = {
            NavigationUpIcon {
                onNavigate(NavigationEvent.Up)
            }
        },
    ) {
        if (uiState.isLoadingAccount) {
            FullScreenLoading()
        }
        else {
            AccountInputForm(
                name = uiState.name,
                onNameChange = { name ->
                    onUiEvent(AccountInputUiEvent.NameChange(name))
                },
                nameError = uiState.nameError?.let { stringResource(it) },
                balance = uiState.balance,
                onBalanceChange = { balance ->
                    onUiEvent(AccountInputUiEvent.BalanceChange(balance))
                },
                balanceError = uiState.balanceError?.let { stringResource(it) },
            )
        }

        if (uiState.showAddMoreDialog) {
            YesNoDialog(
                header = stringResource(R.string.add_more_account),
                yesText = stringResource(R.string.label_yes),
                onYes = {
                    onUiEvent(AccountInputUiEvent.AddMoreAccount)
                },
                noText = stringResource(R.string.label_no),
                onNo = { onNavigate(NavigationEvent.Exit) },
                dismissOnBackPressed = false,
                dismissOnClickOutside = false
            )
        }
    }
}


@Composable
fun FullScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
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


@Composable
fun AccountInputForm(
    name: String,
    onNameChange: (String)-> Unit,
    balance: String,
    onBalanceChange: (String)-> Unit,
    nameError: String? = null,
    balanceError: String? = null,
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = {
                Text(stringResource(R.string.account_name))
            },
            modifier = Modifier.fillMaxWidth(),
            isError = !nameError.isNullOrBlank(),
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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = balance,
            onValueChange = onBalanceChange,
            label = {
                Text(stringResource(R.string.balance))
            },
            modifier = Modifier.fillMaxWidth(),
            isError = balanceError != null,
            supportingText = balanceError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
    }
}

@PreviewLightDark
@Composable
fun AccountInputScreenPreview() {

    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf(AccountInputUiState(isEditing = true, isLoadingAccount = true)) }

    LaunchedEffect(Unit) {
        scope.launch {
            delay(2000)
            uiState = uiState.copy(
                isLoadingAccount = false,
                name = "Wallet",
                balance = "2000.00"
            )
        }
    }

    ExpenseTrackerTheme {
        AccountInputScreen(
            uiState = uiState,
            onNavigate = {},
            onUiEvent = {}
        )
    }
}