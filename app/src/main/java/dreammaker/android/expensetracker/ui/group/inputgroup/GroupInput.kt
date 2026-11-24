package dreammaker.android.expensetracker.ui.group.inputgroup

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import dreammaker.android.expensetracker.ui.component.AmountTextField
import dreammaker.android.expensetracker.ui.component.FullScreenLoading
import dreammaker.android.expensetracker.ui.component.MessageDialog
import dreammaker.android.expensetracker.ui.component.ProgressDialog
import dreammaker.android.expensetracker.ui.component.SnackBarCallback
import dreammaker.android.expensetracker.ui.component.SnackBarVisualsImpl
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import dreammaker.android.expensetracker.ui.navigation.NavigationEvent
import kotlinx.coroutines.delay


@Composable
fun GroupInputRoute(
    snackBarCallback: SnackBarCallback,
    onNavigate: (NavigationEvent)-> Unit,
    viewModel: GroupInputViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when(effect) {
                is GroupInputUiEffect.ShowGroupNotFoundAndExit -> {
                    snackBarCallback(
                        SnackBarVisualsImpl(
                            message = context.getString(R.string.message_group_not_found),
                            duration = SnackbarDuration.Long,
                            withDismissAction = true,
                        ),
                        null,null
                    )
                    onNavigate(NavigationEvent.Exit)
                }
                is GroupInputUiEffect.ShowGroupLoadErrorAndExit -> {
                    snackBarCallback(
                        SnackBarVisualsImpl(
                            message = context.getString(R.string.message_load_error),
                            duration = SnackbarDuration.Long,
                            withDismissAction = true,
                        ),
                        null,null
                    )
                    onNavigate(NavigationEvent.Exit)
                }
                is GroupInputUiEffect.ShowGroupSaveSuccessful -> {
                    snackBarCallback(
                        SnackBarVisualsImpl(context.getString(R.string.message_save_successful)),
                        null,null
                    )

                    if (effect.exit) {
                        onNavigate(NavigationEvent.Exit)
                    }
                }
            }
        }
    }

    GroupInputScreen(
        uiState = uiState,
        onNavUp = { onNavigate(NavigationEvent.Up) },
        onExit = { onNavigate(NavigationEvent.Exit) },
        onUiEvent = { viewModel.handleUiEvent(it) }
    )
}


@Composable
fun GroupInputScreen(
    uiState: GroupInputUiState,
    onNavUp: ()-> Unit,
    onExit: ()-> Unit,
    onUiEvent: (GroupInputUiEvent)-> Unit
) {
    ScreenScaffold(
        navigationIcon = {
            NavigationUpIcon(onClick = onNavUp)
        },
        title = {
            TopBarTitle(
                if (uiState.isEditing) {
                    stringResource(R.string.title_edit_group)
                }
                else {
                    stringResource(R.string.title_create_group)
                }
            )
        },
        actions = {
            if (!uiState.isLoading) {
                TopBarIconAction(
                    Icons.Default.Check,
                    enabled = !uiState.isSaving
                ) {
                    onUiEvent(GroupInputUiEvent.SaveGroup)
                }
            }
        }
    ) {
        if (uiState.isLoading) {
            FullScreenLoading(
                stringResource(R.string.loading_group),
                modifier = Modifier.fillMaxSize().padding(24.dp)
            )
        }
        else {
            GroupInputFormComponent(
                uiState = uiState,
                onUiEvent = onUiEvent
            )
        }

        when (uiState.showDialog) {
            is GroupInputDialogType.SaveError -> {
                GroupSaveErrorDialogComponent(
                    cause = uiState.showDialog.cause,
                    onDismiss = { onUiEvent(GroupInputUiEvent.DismissDialog) }
                )
            }

            is GroupInputDialogType.AddMoreGroup -> {
                AddMoreGroupDialogComponent(
                    onAddMore = { onUiEvent(GroupInputUiEvent.AddMoreGroup) },
                    onDiscard = {
                        onUiEvent(GroupInputUiEvent.DismissDialog)
                        onExit()
                    }
                )
            }

            is GroupInputDialogType.PendingSave -> {
                GroupSavingProgressDialogComponent()
            }

            else -> {}
        }
    }
}

@Composable
fun GroupInputFormComponent(
    uiState: GroupInputUiState,
    onUiEvent: (GroupInputUiEvent)-> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = {
                onUiEvent(GroupInputUiEvent.NameChange(it))
            },
            label = {
                Text(stringResource(R.string.label_group_name))
            },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.nameError != null,
            supportingText = uiState.nameError?.let {{
                Text(stringResource(it))
            }},
            trailingIcon = {
                if (uiState.name.isNotEmpty()) {
                    IconButton(onClick = {
                        onUiEvent(GroupInputUiEvent.NameChange(""))
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        AmountTextField(
            labelText = stringResource(R.string.balance),
            value = uiState.balance,
            onValueChange = {
                onUiEvent(GroupInputUiEvent.BalanceChange(it))
            },
            errorText = uiState.balanceError?.let { stringResource(it) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun GroupSavingProgressDialogComponent() {
    ProgressDialog(body = stringResource(R.string.message_saving_progress))
}

@Composable
fun GroupSaveErrorDialogComponent(
    cause: Throwable,
    onDismiss: ()-> Unit
) {
    MessageDialog(
        body = stringResource(R.string.message_group_save_unsuccessful),
        onDismiss = onDismiss,
        dismissText = stringResource(dreammaker.android.expensetracker.core.R.string.label_ok),
    )
}

@Composable
fun AddMoreGroupDialogComponent(
    onAddMore: ()-> Unit,
    onDiscard: ()-> Unit,
) {
    YesNoDialog(
        body = stringResource(R.string.message_add_more_group),
        onYes = onAddMore,
        onNo = onDiscard,
        onDismissRequest = onDiscard
    )
}

//--- Preview -----------------

@PreviewScreen
@Composable
fun GroupInputFormPreview() {
    var uiState by remember { mutableStateOf(GroupInputUiState(isEditing = true, isLoading = true)) }

    LaunchedEffect(Unit) {
        delay(5000)
        uiState = uiState.copy(
            isLoading = false,
            name = "Monthly Grocery", balance = "2500.00"
        )
    }

    ExpenseTrackerTheme {
        GroupInputScreen(
            uiState = uiState,
            onNavUp = {},
            onExit = {},
            onUiEvent = {}
        )
    }
}
