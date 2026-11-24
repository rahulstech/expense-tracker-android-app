package dreammaker.android.expensetracker.ui.history.historieslist

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.ui.LocalAppColor
import dreammaker.android.expensetracker.core.ui.component.ScreenScaffold
import dreammaker.android.expensetracker.core.ui.component.TopBarIconAction
import dreammaker.android.expensetracker.core.ui.helper.PreviewScreen
import dreammaker.android.expensetracker.ui.component.HistoryTypeChip
import dreammaker.android.expensetracker.ui.component.SnackBarCallback
import dreammaker.android.expensetracker.ui.navigation.NavigationEvent

@Composable
fun HistoryListRoute(
    snackBarCallback: SnackBarCallback,
    onNavigate: (NavigationEvent)-> Unit,
    viewModel: ViewHistoryViewModel = hiltViewModel()
) {

    HistoryListScreen()
}

@Composable
private fun HistoryListScreen(

) {

    var showBottomSheet by remember { mutableStateOf(false) }

    ScreenScaffold(
        actions = {
            TopBarIconAction(painterResource(R.drawable.ic_filter)) {}
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {

                    AssistChip(
                        label = {
                            Text(
                                "Sort",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        trailingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_list_sorting),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = { },
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            trailingIconContentColor = MaterialTheme.colorScheme.onSurface,
                            leadingIconContentColor = MaterialTheme.colorScheme.onSurface,
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            label = {
                                Text(
                                    "May, 2026",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = { showBottomSheet = true },
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                trailingIconContentColor = MaterialTheme.colorScheme.onSurface,
                                leadingIconContentColor = MaterialTheme.colorScheme.onSurface,
                            )
                        )

                        HistoryTypeChip(
                            labelText = "Credit",
                            selected = true,
                            onClick = { },
                            selectedContainerColor = LocalAppColor.current.credit,
                            unselectedContainerColor = MaterialTheme.colorScheme.background,
                            selectedContentColor = LocalAppColor.current.onCredit,
                            unselectedContentColor = MaterialTheme.colorScheme.onBackground,
                        )

                        HistoryTypeChip(
                            labelText = "Debit",
                            selected = true,
                            onClick = { },
                            selectedContainerColor = LocalAppColor.current.debit,
                            unselectedContainerColor = MaterialTheme.colorScheme.background,
                            selectedContentColor = LocalAppColor.current.onDebit,
                            unselectedContentColor = MaterialTheme.colorScheme.onBackground,
                        )

                        HistoryTypeChip(
                            labelText = "Credit",
                            selected = true,
                            onClick = { },
                            selectedContainerColor = LocalAppColor.current.transfer,
                            unselectedContainerColor = MaterialTheme.colorScheme.background,
                            selectedContentColor = LocalAppColor.current.onTransfer,
                            unselectedContentColor = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }

        if (showBottomSheet) {
            DateChooserBottomSheet(
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateChooserBottomSheet(
    onDismiss: ()-> Unit
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                arrayOf("Day","Month","Custom").forEach { labelText ->
                    InputChip(
                        label = {
                            Text(
                                labelText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        selected = true,
                        onClick = { },
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            labelColor = MaterialTheme.colorScheme.onBackground,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
        }
    }
}







@PreviewScreen
@Composable
private fun HistoryListScreenPreview() {
    ExpenseTrackerTheme {
        HistoryListScreen()
    }
}