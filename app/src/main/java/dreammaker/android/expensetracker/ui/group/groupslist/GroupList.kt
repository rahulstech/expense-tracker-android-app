package dreammaker.android.expensetracker.ui.group.groupslist

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import dreammaker.android.expensetracker.core.ui.component.NavigationUpIcon
import dreammaker.android.expensetracker.core.ui.component.ScreenScaffold
import dreammaker.android.expensetracker.core.ui.helper.PreviewScreen
import dreammaker.android.expensetracker.core.ui.helper.shimmer
import dreammaker.android.expensetracker.ui.component.ETSearchField
import dreammaker.android.expensetracker.ui.component.MessageDialog
import dreammaker.android.expensetracker.ui.component.ProgressDialog
import dreammaker.android.expensetracker.ui.component.SnackBarCallback
import dreammaker.android.expensetracker.ui.component.SnackBarVisualsImpl
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import dreammaker.android.expensetracker.ui.navigation.AppDestination
import dreammaker.android.expensetracker.ui.navigation.NavigationEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Group
import java.math.BigDecimal

@Composable
fun GroupListRoute(
    snackBarCallback: SnackBarCallback,
    onNavigation: (NavigationEvent)-> Unit,
    viewModel: GroupListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when(effect) {
                is GroupListUiEffect.ShowDeleteGroupsSuccessful -> {
                    snackBarCallback(
                        SnackBarVisualsImpl(
                            message = context.resources.getQuantityString(
                                R.plurals.message_group_delete_successful, Int.MAX_VALUE
                            ),
                            withDismissAction = true,
                            duration = SnackbarDuration.Long
                        ),
                        null, null
                    )
                }
            }
        }
    }

    GroupListScreen(
        uiState = uiState,
        onNavUp = {
            onNavigation(NavigationEvent.Up)
        },
        onCreateGroup = {
            onNavigation(NavigationEvent.To(AppDestination.AddGroup))
        },
        onViewGroup = {
            onNavigation(NavigationEvent.To(AppDestination.ViewGroup(it.id)))
        },
        onUiEvent = {
            viewModel.handleUiEvent(it)
        }
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupListScreen(
    uiState: GroupListUiState,
    onNavUp: ()-> Unit,
    onCreateGroup: ()-> Unit,
    onViewGroup: (Group)-> Unit,
    onUiEvent: (GroupListUiEvent)-> Unit,
) {
    ScreenScaffold(
        title = {
            if (uiState.isSelectingGroups) {
                Text(text = uiState.groupSelectionCount.toString())
            }
            else {
                Text(text = stringResource(R.string.title_groups_list))
            }
        },
        actions = {
            if (uiState.isSelectingGroups) {
                IconButton(onClick = { onUiEvent(GroupListUiEvent.DeleteSelectedGroups) }) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        },
        navigationIcon = {
            if (uiState.isSelectingGroups) {
                IconButton(onClick = { onUiEvent(GroupListUiEvent.ExitGroupSelection) }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.label_exit_selection))
                }
            }
            else {
                NavigationUpIcon(onClick = onNavUp)
            }
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = onCreateGroup,
                    shape = MaterialTheme.shapes.extraLarge,
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    text = {
                        Text(stringResource(R.string.add_new_group))
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {

        if (uiState.isLoading) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(count = 10, key = { -(it+1)*31 }) {
                    GroupLoadingItemComponent()
                }
            }
        }
        else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {

                item {
                    ETSearchField(
                        modifier = Modifier.fillMaxWidth()
                            .padding(16.dp),
                        value = uiState.queryText,
                        onValueChange = {
                            onUiEvent(GroupListUiEvent.GroupSearchTextChange(it))
                        },
                        placeholderText = stringResource(R.string.label_search_group_name)
                    )
                }

                item {
                    if (uiState.allGroups.isEmpty()) {
                        EmptyGroupListComponent()
                    }
                }

                items(items = uiState.allGroups, key = { it.id }) { group ->
                    GroupItemComponent(
                        group = group,
                        isSelected = uiState.isGroupSelected(group),
                        onClick = {
                            if (uiState.isSelectingGroups) {
                                onUiEvent(GroupListUiEvent.ToggleGroupSelection(it))
                            }
                            else {
                                onViewGroup(it)
                            }
                        },
                        onLongClick = {
                            onUiEvent(GroupListUiEvent.ToggleGroupSelection(it))
                        }
                    )
                }
            }
        }

        when(uiState.showDialog) {
            is GroupListDialog.ConfirmDelete -> {
                ConfirmDeleteDialogComponent(
                    selectionCount = uiState.groupSelectionCount,
                    onDelete = {
                        onUiEvent(GroupListUiEvent.ConfirmDeleteSelectedGroup)
                    },
                    onCancel = {
                        onUiEvent(GroupListUiEvent.ExitGroupSelection)
                    }
                )
            }

            is GroupListDialog.PendingDelete -> {
                PendingDeleteDialogComponent()
            }

            is GroupListDialog.DeleteError -> {
                DeleteErrorDialogComponent(
                    cause = uiState.showDialog.cause,
                    onDismiss = { onUiEvent(GroupListUiEvent.DismissDialog) }
                )
            }

            else -> {}
        }
    }
}


@Composable
fun GroupLoadingItemComponent() {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(.7f)
                .height(28.dp)
                .shimmer()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth(.45f)
                .height(24.dp)
                .shimmer()
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(thickness = 1.dp, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun EmptyGroupListComponent() {
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(top = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.label_empty_group_list),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupItemComponent(
    group: Group,
    isSelected: Boolean,
    onClick: (Group)-> Unit,
    onLongClick: (Group)-> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = .3f) else Color.Transparent
            )
            .combinedClickable(
                enabled = true,
                onClick = { onClick(group) },
                onLongClick = { onLongClick(group) }
            )
            .padding(top = 12.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            group.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            group.balance.toString(), // TODO: use current format and text color for group balance in group list item
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ConfirmDeleteDialogComponent(
    selectionCount: Int,
    onDelete: ()-> Unit,
    onCancel: ()-> Unit
) {
    YesNoDialog(
        body = pluralStringResource(R.plurals.message_warning_delete_group, Int.MAX_VALUE, selectionCount),
        yesText = stringResource(R.string.cancel),
        onYes = onCancel,
        noText = stringResource(R.string.delete),
        onNo = onDelete
    )
}

@Composable
private fun PendingDeleteDialogComponent() {
    ProgressDialog(
        body = pluralStringResource(R.plurals.message_group_delete_progress, Int.MAX_VALUE),
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )
}

@Composable
private fun DeleteErrorDialogComponent(
    cause: Throwable,
    onDismiss: ()-> Unit,
) {
    MessageDialog(
        body = pluralStringResource(R.plurals.message_group_delete_unsuccessful, Int.MAX_VALUE),
        onDismiss = onDismiss
    )
}

@PreviewScreen
@Composable
fun GroupListScreenPreview() {

    val allGroups = remember { listOf(
        Group(name = "Grocery", balance = BigDecimal("5500.00"), id = 1L ),
        Group(name = "Medicine", balance = BigDecimal("3000.83"), id = 2L),
        Group(name = "EMI", balance = BigDecimal("12500.00"), id = 3L)
    ) }
    var uiState by  remember { mutableStateOf(GroupListUiState(isLoading = true)) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(5000)
        uiState = uiState.copy(
            isLoading = false,
            allGroups = allGroups
        )
    }

    ExpenseTrackerTheme {
        GroupListScreen(
            uiState = uiState,
            onNavUp = {},
            onCreateGroup = {},
            onViewGroup = {},
            onUiEvent = { event ->
                when(event) {
                    is GroupListUiEvent.GroupSearchTextChange -> {
                        scope.launch(Dispatchers.IO) {
                            uiState = uiState.copy(
                                queryText = event.queryText,
                                allGroups = allGroups.filter {
                                    it.name.contains(event.queryText, ignoreCase = true)
                                }
                            )
                        }

                    }
                    is GroupListUiEvent.ToggleGroupSelection -> {
                        uiState = uiState.copy(isSelectingGroups = true)
                            .toggleGroupSelection(event.group)
                    }
                    is GroupListUiEvent.ExitGroupSelection -> {
                        uiState = uiState.copy(isSelectingGroups = false, showDialog = GroupListDialog.None)
                            .clearGroupSelection()
                    }
                    is GroupListUiEvent.DeleteSelectedGroups -> {
                        uiState = uiState.copy(showDialog = GroupListDialog.ConfirmDelete)
                    }
                    is GroupListUiEvent.ConfirmDeleteSelectedGroup -> {
                        scope.launch(Dispatchers.IO) {
                            uiState = uiState.copy(showDialog = GroupListDialog.PendingDelete)
                            delay(5000)
                            uiState = uiState.copy(showDialog = GroupListDialog.None)
                        }
                    }
                    else -> {}
                }
            }
        )
    }
}