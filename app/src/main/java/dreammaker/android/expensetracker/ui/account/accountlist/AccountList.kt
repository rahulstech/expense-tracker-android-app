package dreammaker.android.expensetracker.ui.account.accountlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.ui.component.ScreenScaffold
import dreammaker.android.expensetracker.core.ui.helper.PreviewScreen
import dreammaker.android.expensetracker.core.ui.helper.shimmer
import dreammaker.android.expensetracker.ui.component.ETSearchField
import dreammaker.android.expensetracker.ui.component.MessageDialog
import dreammaker.android.expensetracker.ui.component.SnackBarCallback
import dreammaker.android.expensetracker.ui.component.SnackBarVisualsImpl
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import dreammaker.android.expensetracker.ui.navigation.AppDestination
import dreammaker.android.expensetracker.ui.navigation.NavigationEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Account
import java.math.BigDecimal

@Composable
fun AccountListRoute(
    snackBarCallback: SnackBarCallback,
    onNavigate: (NavigationEvent)-> Unit,
    viewModel: AccountsListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
   val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when(effect) {
                is AccountListUiEffect.ShowDeleteAccountsSuccessful -> {
                    snackBarCallback(
                        SnackBarVisualsImpl(
                            message = context.resources.getQuantityString(R.plurals.message_account_delete_successful,Int.MAX_VALUE),
                            withDismissAction = true,
                            duration = SnackbarDuration.Long
                        ),
                        null, null
                    )
                }
            }
        }
    }

    AccountListScreen(
        uiState = uiState,
        onNavUp = { onNavigate(NavigationEvent.Up) },
        onAddAccount = { onNavigate(NavigationEvent.To(AppDestination.AddAccount))},
        onViewAccount = { account ->
            onNavigate(NavigationEvent.To(AppDestination.ViewAccount(account.id)))
        },
        onUiEvent = {
            viewModel.handleEvent(it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AccountListScreen(
    uiState: AccountListUiState,
    onNavUp: ()-> Unit,
    onAddAccount: ()-> Unit,
    onViewAccount: (Account)-> Unit,
    onUiEvent: (AccountListUiEvent)-> Unit
) {
    ScreenScaffold(
        title = {
            if (uiState.isSelectingAccounts) {
                Text(text = uiState.accountSelectionCount.toString())
            }
            else {
                Text(text = stringResource(R.string.title_account_list))
            }
        },
        actions = {
            if (uiState.isSelectingAccounts) {
                IconButton(onClick = { onUiEvent(AccountListUiEvent.DeleteSelectedAccounts) }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.label_exit_selection))
                }
            }
        },
        navigationIcon = {
            if (uiState.isSelectingAccounts) {
                IconButton(onClick = { onUiEvent(AccountListUiEvent.ExitAccountsSelection) }) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
            else {
                IconButton(onClick = onNavUp) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                }
            }
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = onAddAccount,
                    shape = MaterialTheme.shapes.extraLarge,
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    text = {
                        Text(text = stringResource(R.string.label_add_account))
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {
        if (uiState.isLoading) {
            LazyColumn {
                items(count = 10, key = { -(it*1)*31}) {
                    AccountLoadingItemComponent()
                }
            }
        }
        else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {

                item {
                    ETSearchField(
                        value = uiState.queryText,
                        onValueChange = { onUiEvent(AccountListUiEvent.AccountSearchTextChange(it)) },
                        placeholderText = stringResource(R.string.label_search_account_name),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }

                item {
                    if (uiState.allAccounts.isEmpty()) {
                        EmptyAccountListComponent()
                    }
                }

                items(items=uiState.allAccounts, key = { it.id }) { account ->
                    AccountItemComponent(
                        account,
                        uiState.isAccountSelected(account),
                        onClick = {
                            if (uiState.isSelectingAccounts) {
                                onUiEvent(AccountListUiEvent.ToggleAccountSelection(it))
                            }
                            else {
                                onViewAccount(it)
                            }
                        },
                        onLongClick = { onUiEvent(AccountListUiEvent.ToggleAccountSelection(it))}
                    )
                }
            }
        }

        when (uiState.showDialog) {
            is AccountListDialog.ConfirmDelete -> {
                ConfirmDeleteDialogComponent(
                    selectionCount = uiState.accountSelectionCount,
                    onConfirm = { onUiEvent(AccountListUiEvent.DeleteSelectedAccounts) },
                    onDiscard = { onUiEvent(AccountListUiEvent.ExitAccountsSelection) }
                )
            }

            is AccountListDialog.PendingDelete -> {
                PendingDeleteDialogComponent()
            }

            is AccountListDialog.DeleteError -> {
                DeleteErrorDialogComponent(
                    cause = uiState.showDialog.cause,
                    onDismiss = { onUiEvent(AccountListUiEvent.DismissDialog) }
                )
            }

            else -> {}
        }
    }
}

@Composable
fun AccountLoadingItemComponent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
    ) {
        Box(modifier = Modifier.height(24.dp)
            .fillMaxWidth(fraction = .6f)
            .shimmer()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.height(20.dp)
            .fillMaxWidth(fraction = .4f)
            .shimmer()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun EmptyAccountListComponent() {
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(top = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.label_empty_account_list), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.outline)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountItemComponent(
    account: Account,
    isSelected: Boolean,
    onClick: (Account)-> Unit,
    onLongClick: (Account)-> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = .3f) else Color.Transparent
            )
            .combinedClickable(
                enabled = true,
                onClick = { onClick(account) },
                onLongClick = { onLongClick(account) }
            )
            .padding(top = 12.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            account.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            account.balance.toString(),  // TODO: use current format and text color for account balance in account list item
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}


@Composable
fun ConfirmDeleteDialogComponent(
    selectionCount: Int,
    onConfirm: ()-> Unit,
    onDiscard: ()-> Unit
) {
    YesNoDialog(
        body = pluralStringResource(R.plurals.message_warning_delete_account, Int.MAX_VALUE, selectionCount),
        yesText = stringResource(R.string.label_no),
        onYes = onDiscard,
        noText = stringResource(R.string.label_yes),
        onNo = onConfirm,
    )
}

@Composable
fun PendingDeleteDialogComponent() {
    MessageDialog(
        body = pluralStringResource(R.plurals.message_account_delete_progress, Int.MAX_VALUE),
        onDismiss = {},
        dismissOnBackPressed = false,
        dismissOnClickOutside = false,
    )
}

@Composable
fun DeleteErrorDialogComponent(
    cause: Throwable,
    onDismiss: () -> Unit
) {
    MessageDialog(
        body = pluralStringResource(R.plurals.message_account_delete_unsuccessful, Int.MAX_VALUE),
        onDismiss = onDismiss
    )
}


@PreviewScreen
@Composable
fun AccountListScreenPreview() {

    val allAccount = remember { listOf(
        Account(name = "Wallet", balance = BigDecimal("1500.00"), id = 1L ),
        Account(name = "Savings Bank Account", balance = BigDecimal("39560.83"), id = 2L),
        Account(name = "CA Account", balance = BigDecimal("150362.00"), id = 3L)
    ) }
    var uiState by  remember { mutableStateOf(AccountListUiState(isLoading = true)) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(5000)
        uiState = uiState.copy(
            isLoading = false,
            allAccounts = allAccount
        )
    }

    ExpenseTrackerTheme {
        AccountListScreen(
            uiState = uiState,
            onNavUp = {},
            onAddAccount = {},
            onViewAccount = {},
            onUiEvent = { event ->
                when(event) {
                    is AccountListUiEvent.AccountSearchTextChange -> {
                        scope.launch(Dispatchers.IO) {
                            uiState = uiState.copy(
                                queryText = event.queryText,
                                allAccounts = allAccount.filter {
                                    it.name.contains(event.queryText, ignoreCase = true)
                                }
                            )
                        }

                    }
                    is AccountListUiEvent.ToggleAccountSelection -> {
                        uiState = uiState.copy(isSelectingAccounts = true).toggleAccountSelection(event.account)
                    }
                    is AccountListUiEvent.ExitAccountsSelection -> {
                        uiState = uiState.copy(isSelectingAccounts = false).clearAccountSelection()
                    }
                    else -> {}
                }
            }
        )
    }
}