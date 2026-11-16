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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
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
import dreammaker.android.expensetracker.ui.component.MessageDialog
import dreammaker.android.expensetracker.ui.component.ProgressDialog
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import dreammaker.android.expensetracker.ui.navigation.NavigationEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun AccountInputRoute(
    snackBarHostState: SnackbarHostState,
    onNavigate: (NavigationEvent)-> Unit,
    viewModel: AccountInputViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when(effect) {
                is AccountInputUiEffect.ShowAccountLoadErrorAndExit -> {
                    snackBarHostState.showSnackbar(
                        message = context.getString(R.string.message_load_error),
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                }

                is AccountInputUiEffect.ShowAccountNotFoundAndExit -> {
                    snackBarHostState.showSnackbar(
                        message = context.getString(R.string.message_account_not_found),
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                }

                is AccountInputUiEffect.ShowSaveSuccessful -> {
                    snackBarHostState.showSnackbar(
                        message = context.getString(R.string.message_save_successful),
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    AccountInputScreen(
        uiState = uiState,
        onNavUp = { onNavigate(NavigationEvent.Up) },
        onExit = { onNavigate(NavigationEvent.Exit) },
        onUiEvent = { viewModel.handleUiEvent(it) }
    )
}

@Composable
fun AccountInputScreen(
    uiState: AccountInputUiState,
    onNavUp: ()-> Unit,
    onExit: ()-> Unit,
    onUiEvent: (AccountInputUiEvent)-> Unit
) {
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
            if (!uiState.isLoading) {
                TopBarIconAction(
                    Icons.Default.Done,
                    title = stringResource(R.string.save),
                    enabled = !uiState.isSaving
                ) {
                    onUiEvent(AccountInputUiEvent.SaveAccount)
                }
            }
        },
        navigationIcon = {
            NavigationUpIcon(onClick = onNavUp)
        },
    ) {
        if (uiState.isLoading) {
            FullScreenLoadingComponent()
        }
        else {
            AccountInputFormComponent(
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

        when (uiState.showDialog) {

            is AccountInputDialog.PendingSave -> {
                PendingSaveDialogComponent()
            }

            is AccountInputDialog.SaveError -> {
                SaveErrorDialogComponent(
                    cause = uiState.showDialog.cause,
                    onDismiss = { onUiEvent(AccountInputUiEvent.DismissDialog) },
                )
            }

            is AccountInputDialog.AddMore -> {
                AddMoreDialogComponent(
                    onAddMore = { onUiEvent(AccountInputUiEvent.AddMoreAccount) },
                    onDiscard = { onExit() }
                )
            }

            else -> {}
        }
    }
}


@Composable
fun FullScreenLoadingComponent() {
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
fun AccountInputFormComponent(
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


@Composable
fun PendingSaveDialogComponent() {
    ProgressDialog(body = stringResource(R.string.message_saving_progress))
}

@Composable
fun SaveErrorDialogComponent(
    cause: Throwable,
    onDismiss: ()-> Unit
) {
    MessageDialog(
        body = stringResource(R.string.message_account_save_unsuccessful),
        onDismiss = onDismiss,
        dismissText = stringResource(dreammaker.android.expensetracker.core.R.string.label_ok),
    )
}

@Composable
fun AddMoreDialogComponent(
    onAddMore: ()-> Unit,
    onDiscard: ()-> Unit,
) {
    YesNoDialog(
        body = stringResource(R.string.message_add_more_account),
        onYes = onAddMore,
        onNo = onDiscard,
        onDismissRequest = onDiscard
    )
}

@PreviewLightDark
@Composable
fun AccountInputScreenPreview() {

    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf(AccountInputUiState(isEditing = true, isLoading = true)) }

    LaunchedEffect(Unit) {
        scope.launch {
            delay(2000)
            uiState = uiState.copy(
                isLoading = false,
                name = "Wallet",
                balance = "2000.00"
            )
        }
    }

    ExpenseTrackerTheme {
        AccountInputScreen(
            uiState = uiState,
            onNavUp = {},
            onExit = {},
            onUiEvent = {}
        )
    }
}