package dreammaker.android.expensetracker.ui.group.viewgroup

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.ui.component.NavigationUpIcon
import dreammaker.android.expensetracker.core.ui.component.ScreenScaffold
import dreammaker.android.expensetracker.core.ui.component.TopBarIconAction
import dreammaker.android.expensetracker.core.ui.component.TopBarTitle
import dreammaker.android.expensetracker.core.ui.helper.PreviewScreen
import dreammaker.android.expensetracker.core.ui.helper.shimmer
import dreammaker.android.expensetracker.ui.account.viewaccount.DeleteErrorDialogComponent
import dreammaker.android.expensetracker.ui.account.viewaccount.PendingDeleteDialogComponent
import dreammaker.android.expensetracker.ui.account.viewaccount.ViewAccountScreen
import dreammaker.android.expensetracker.ui.account.viewaccount.ViewAccountUiState
import dreammaker.android.expensetracker.ui.component.ETOutlinedIconButton
import dreammaker.android.expensetracker.ui.component.MessageDialog
import dreammaker.android.expensetracker.ui.component.ProgressDialog
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import dreammaker.android.expensetracker.ui.navigation.AppDestination
import dreammaker.android.expensetracker.ui.navigation.NavigationEvent
import dreammaker.android.expensetracker.util.toCurrencyString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group

@Composable
fun ViewGroupRoute(
    snackBarHostState: SnackbarHostState,
    onNavigate: (NavigationEvent)-> Unit,
    viewModel: ViewGroupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when(effect) {
                is ViewGroupUiEffect.ShowGroupLoadErrorAndExit -> {
                    onNavigate(NavigationEvent.Exit)
                    snackBarHostState.showSnackbar(
                        message = context.getString(R.string.message_load_error),
                        duration = SnackbarDuration.Long
                    )
                }

                is ViewGroupUiEffect.ShowGroupNotFoundAndExit -> {
                    onNavigate(NavigationEvent.Exit)
                    snackBarHostState.showSnackbar(
                        message = context.getString(R.string.message_group_not_found),
                        duration = SnackbarDuration.Long
                    )
                }

                is ViewGroupUiEffect.ShowDeleteGroupSuccessfulAndExit -> {
                    onNavigate(NavigationEvent.Exit)
                    snackBarHostState.showSnackbar(
                        message = context.resources.getQuantityString(
                            R.plurals.message_account_delete_successful, 1,
                            effect.group.name
                        ),
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    ViewGroupScreen(
        uiState = uiState,
        onNavUp = {
            onNavigate(NavigationEvent.Up)
        },
        onEditGroup = {
            onNavigate(NavigationEvent.To(AppDestination.EditGroup(it.id)))
        },
        onAddHistory = {
            onNavigate(NavigationEvent.To(AppDestination.AddGroupHistory(it.id)))
        },
        onViewHistory = {
            onNavigate(NavigationEvent.To(AppDestination.ViewGroupHistory(it.id)))
        },
        onUiEvent = viewModel::handleUiEvent
    )
}

@Composable
fun ViewGroupScreen(
    uiState: ViewGroupUiState,
    onNavUp: ()-> Unit,
    onEditGroup: (Group)-> Unit,
    onAddHistory: (Group)-> Unit,
    onViewHistory: (Group)-> Unit,
    onUiEvent: (ViewGroupUiEvent)-> Unit,
) {

    ScreenScaffold (
        title = {
            TopBarTitle(stringResource(R.string.title_view_group))
        },
        actions = {
            if (!uiState.isLoading) {
                TopBarIconAction(Icons.Default.Delete) {
                    onUiEvent(ViewGroupUiEvent.DeleteGroup)
                }

                TopBarIconAction(Icons.Default.Edit) {
                    onEditGroup(uiState.group)
                }
            }
        },
        navigationIcon = { NavigationUpIcon(onClick = onNavUp) },
        floatingActionButton = {
            if (!uiState.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = { onAddHistory(uiState.group)},
                    shape = MaterialTheme.shapes.extraLarge,
                    text = {
                        Text(text = stringResource(R.string.label_add_history))
                    },
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {
        if (uiState.isLoading) {
            GroupLoadingComponent()
        }
        else {
            GroupDetailsComponent(
                uiState.group,
                onViewGroupHistory = { onViewHistory(uiState.group) },
            )
        }

        when(uiState.showDialog) {
            is ViewGroupDialog.ConfirmDelete -> {
                ConfirmDeleteDialogComponent(
                    group = uiState.group,
                    onDelete = {
                        onUiEvent(ViewGroupUiEvent.ConfirmDeleteGroup)
                    },
                    onDiscard = {
                        onUiEvent(ViewGroupUiEvent.DismissDialog)
                    }
                )
            }

            is ViewGroupDialog.PendingDelete -> {
                PendingDeleteDialogComponent()
            }

            is ViewGroupDialog.DeleteError -> {
                DeleteErrorDialogComponent(
                    cause = uiState.showDialog.cause,
                    onDismiss = { onUiEvent(ViewGroupUiEvent.DismissDialog) }
                )
            }

            else -> {}
        }
    }
}





@Composable
fun GroupLoadingComponent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(modifier = Modifier.fillMaxWidth()
            .height(28.dp)
            .shimmer()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth(.6f)
            .height(24.dp)
            .shimmer()
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier.size(64.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .shimmer()
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier.size(64.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .shimmer()
            )
        }
    }
}

@Composable
fun GroupDetailsComponent(
    group: Group,
    onViewGroupHistory: (Group)-> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(group.name, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(12.dp))

        Text(group.balance.toCurrencyString(), style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            ETOutlinedIconButton(
                onClick = { onViewGroupHistory(group) },
                icon = painterResource(R.drawable.ic_history),
                text = stringResource(R.string.label_view_history)
            )
        }
    }
}


@Composable
fun ConfirmDeleteDialogComponent(
    group: Group,
    onDelete: ()-> Unit,
    onDiscard: ()-> Unit
) {
    YesNoDialog(
        header = pluralStringResource(R.plurals.message_warning_delete_account, 1, group.name),
        yesText = stringResource(R.string.label_no),
        onYes = onDiscard,
        noText = stringResource(R.string.label_yes),
        onNo = onDelete,
        onDismissRequest = onDiscard
    )
}

@Composable
fun PendingDeleteDialogComponent() {
    ProgressDialog(
        body = pluralStringResource(R.plurals.message_group_delete_progress, 1),
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )
}

@Composable
fun DeleteErrorDialogComponent(
    cause: Throwable,
    onDismiss: () -> Unit
) {
    MessageDialog(
        body = pluralStringResource(R.plurals.message_account_delete_unsuccessful, 1),
        onDismiss = onDismiss
    )
}



@PreviewScreen
@Composable
fun ViewAccountScreenPreview() {

    var uiState by remember{ mutableStateOf(ViewAccountUiState(isLoading = true)) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            delay(2000)
            uiState = uiState.copy(isLoading = false, account = Account("Wallet", 2000000.00, 1))
        }
    }

    ExpenseTrackerTheme {
        ViewAccountScreen(
            uiState = uiState,
            onNavUp = {},
            onEditAccount = {},
            onViewHistory = {},
            onSendMoney = {},
            onAddHistory = {},
            onUiEvent = {}
        )
    }
}