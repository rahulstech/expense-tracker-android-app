package dreammaker.android.expensetracker.ui.history.historyinput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.ui.component.NavigationUpIcon
import dreammaker.android.expensetracker.core.ui.component.ScreenScaffold
import dreammaker.android.expensetracker.core.ui.component.TopBarIconAction
import dreammaker.android.expensetracker.core.ui.helper.PreviewScreen
import dreammaker.android.expensetracker.ui.component.DatePicker
import dreammaker.android.expensetracker.ui.component.FullScreenLoading
import dreammaker.android.expensetracker.ui.component.SnackBarCallback
import dreammaker.android.expensetracker.ui.component.SnackBarVisualsImpl
import dreammaker.android.expensetracker.ui.navigation.AppDestination
import dreammaker.android.expensetracker.ui.navigation.NavigationEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.HistoryType
import java.time.LocalDate


@Composable
fun TransferInputRoute(
    snackBarCallback: SnackBarCallback,
    onNavigation: (NavigationEvent)-> Unit,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when(effect) {
                is HistoryInputUiEffect.ShowLoadErrorAndExit -> {
                    snackBarCallback(
                        SnackBarVisualsImpl(context.getString(R.string.message_load_error), withDismissAction = true),
                        null, null
                    )
                    onNavigation(NavigationEvent.Exit)
                }
                is HistoryInputUiEffect.ShowNotFoundAndExit -> {
                    snackBarCallback(
                        SnackBarVisualsImpl(context.getString(R.string.message_history_not_found), withDismissAction = true),
                        null, null
                    )
                    onNavigation(NavigationEvent.Exit)
                }
                is HistoryInputUiEffect.ShowSaveSuccessful -> {
                    snackBarCallback(
                        SnackBarVisualsImpl(context.getString(R.string.message_save_successful)),
                        null, null
                    )
                    if (effect.exit) {
                        onNavigation(NavigationEvent.Exit)
                    }
                }
            }
        }
    }

    TransferInputScreen(
        uiState = uiState,
        onNavUp = { onNavigation(NavigationEvent.Up) },
        onExit = { onNavigation(NavigationEvent.Exit) },
        onAddNewAccount = { onNavigation(NavigationEvent.To(AppDestination.AddAccount)) },
        onUiEvent = { viewModel.handleUiEvent(it) }
    )
}

@Composable
fun TransferInputScreen(
    uiState: HistoryInputUiState,
    onNavUp: ()-> Unit,
    onExit: ()-> Unit,
    onAddNewAccount: () -> Unit,
    onUiEvent: (HistoryInputUiEvent)-> Unit,
) {
    ScreenScaffold(
        navigationIcon = {
            NavigationUpIcon(onClick = onNavUp)
        },

        actions = {
            if (!uiState.isLoading) {
                TopBarIconAction(
                    Icons.Default.Check,
                    enabled = !uiState.isSaving,
                    onClick = { onUiEvent(HistoryInputUiEvent.Save) }
                )
            }
        }
    ) {
        if (uiState.isLoading) {
            FullScreenLoading(
                stringResource(R.string.loading_history),
                modifier = Modifier.fillMaxSize()
                    .padding(24.dp)
            )
        } else {
            TransferInputForm(
                uiState = uiState,
                onUiEvent = onUiEvent,
                onAddNewAccount = onAddNewAccount,
            )
        }

        HistoryInputDialogComponent(
            dialog = uiState.dialog,
            onDismiss =  { exit ->
                onUiEvent(HistoryInputUiEvent.DismissDialog)
                if (exit) onExit()
            },
            onAddMore = {
                onUiEvent(HistoryInputUiEvent.AddMore)
            }
        )
    }
}

@Composable
private fun TransferInputForm(
    uiState: HistoryInputUiState,
    onUiEvent: (HistoryInputUiEvent) -> Unit,
    onAddNewAccount: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        item {
            DatePicker(
                selectedDate = uiState.date,
                onDateSelected = { onUiEvent(HistoryInputUiEvent.DateChange(it)) }
            )
        }

        item {
            AmountSection(
                amount = uiState.amount,
                onAmountChange = { onUiEvent(HistoryInputUiEvent.AmountChange(it)) },
                amountError = uiState.amountError?.let{ stringResource(it) },
            )
        }

        item {
            NotesSection(
                notes = uiState.note,
                onNotesChange = { onUiEvent(HistoryInputUiEvent.NoteChange(it)) }
            )
        }

        item {
            AccountSelectionSection(
                labelText = stringResource(R.string.label_from_account),
                data = SelectionDropdownData(
                    isLoading = uiState.isLoading,
                    options = uiState.allAccounts,
                    quickOptions = uiState.recentAccounts
                ),
                selectedAccount = uiState.sourceAccount,
                onAccountSelected = { onUiEvent(HistoryInputUiEvent.SourceAccountChange(it)) },
                onAddNewAccount = onAddNewAccount,
                errorText = uiState.sourceAccountError?.let { stringResource(it) }
            )
        }

        item {
            AccountSelectionSection(
                labelText = stringResource(R.string.label_in_account),
                data = SelectionDropdownData(
                    isLoading = uiState.isLoading,
                    options = uiState.allAccounts,
                    quickOptions = uiState.recentAccounts
                ),
                selectedAccount = uiState.destinationAccount,
                onAccountSelected = { onUiEvent(HistoryInputUiEvent.DestinationAccountChange(it)) },
                onAddNewAccount = onAddNewAccount,
                errorText = uiState.destinationAccountError?.let { stringResource(it) }
            )
        }
    }
}

//-----------------------------------------
//      Preview
//-----------------------------------------

@PreviewScreen
@Composable
fun TransferInputScreenPreview() {
    val accounts = listOf(
        Account(name = "Main Checking (***4920)", id = 1),
        Account(name = "Savings", id = 2),
        Account(name = "Credit Card", id = 3)
    )
    var uiState by remember {
        mutableStateOf(
            HistoryInputUiState(
                isEditing = true,
                isLoading = true,
                id = 0L,
                date = LocalDate.now(),
                group = null,
                sourceAccount = null,
                amount = "",
                type = HistoryType.DEBIT,
                note = "",
            )
        )
    }

    LaunchedEffect(Unit) {
        launch {
            delay(2000)
            uiState = uiState.copy(
                isLoading = false,
                id = 1L,
                date = LocalDate.now(),
                sourceAccount = accounts[0],
                destinationAccount = accounts[2],
                amount = "125.50",
                type = HistoryType.DEBIT,
                note = "Grocery shopping at local market",
            )
        }

        launch {
            delay(5000)
            uiState = uiState.copy(
                isLoading = false,
                recentAccounts = listOf(accounts[0],accounts[2]),
                allAccounts = accounts
            )
        }
    }

    ExpenseTrackerTheme {
        TransferInputScreen(
            uiState = uiState,
            onNavUp = {},
            onExit = {},
            onAddNewAccount = {},
            onUiEvent = {}
        )
    }
}
