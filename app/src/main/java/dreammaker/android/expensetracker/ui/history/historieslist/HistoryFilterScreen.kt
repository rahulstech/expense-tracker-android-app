package dreammaker.android.expensetracker.ui.history.historieslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.ui.component.DatePicker
import dreammaker.android.expensetracker.ui.component.MonthPicker
import dreammaker.android.expensetracker.util.ViewHistory
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate
import java.time.YearMonth

data class HistoryFilterUiState(
    val viewHistory: ViewHistory = ViewHistory.MONTHLY,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedTypes: List<History.Type> = History.Type.entries
)

@Composable
fun HistoryFilterScreen(
    onFilterChanged: (ViewHistory, LocalDate?, YearMonth?, List<History.Type>) -> Unit,
    modifier: Modifier = Modifier,
    initialViewHistory: ViewHistory = ViewHistory.MONTHLY,
    initialSelectedTypes: List<History.Type> = History.Type.entries
) {
    var uiState by remember {
        mutableStateOf(
            HistoryFilterUiState(
                viewHistory = initialViewHistory,
                selectedTypes = initialSelectedTypes
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        HistoryDateFilter(
            viewHistory = uiState.viewHistory,
            selectedDate = uiState.selectedDate,
            selectedMonth = uiState.selectedMonth,
            onViewHistorySelected = { newViewHistory ->
                uiState = uiState.copy(viewHistory = newViewHistory)
                onFilterChanged(
                    newViewHistory,
                    if (newViewHistory == ViewHistory.DAILY) uiState.selectedDate else null,
                    if (newViewHistory == ViewHistory.MONTHLY) uiState.selectedMonth else null,
                    uiState.selectedTypes
                )
            },
            onDateSelected = { newDate ->
                uiState = uiState.copy(selectedDate = newDate)
                onFilterChanged(uiState.viewHistory, newDate, null, uiState.selectedTypes)
            },
            onMonthSelected = { newMonth ->
                uiState = uiState.copy(selectedMonth = newMonth)
                onFilterChanged(uiState.viewHistory, null, newMonth, uiState.selectedTypes)
            }
        )

        HistoryTypeFilter(
            selectedTypes = uiState.selectedTypes,
            onToggleType = { type ->
                val newTypes = if (uiState.selectedTypes.contains(type)) {
                    uiState.selectedTypes - type
                } else {
                    uiState.selectedTypes + type
                }
                uiState = uiState.copy(selectedTypes = newTypes)
                onFilterChanged(
                    uiState.viewHistory,
                    if (uiState.viewHistory == ViewHistory.DAILY) uiState.selectedDate else null,
                    if (uiState.viewHistory == ViewHistory.MONTHLY) uiState.selectedMonth else null,
                    newTypes
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDateFilter(
    viewHistory: ViewHistory,
    selectedDate: LocalDate,
    selectedMonth: YearMonth,
    onViewHistorySelected: (ViewHistory) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val options = listOf(
            ViewHistory.DAILY to stringResource(R.string.menu_title_view_history_daily),
            ViewHistory.MONTHLY to stringResource(R.string.menu_title_view_history_monthly)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEachIndexed { index, (type, label) ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = { onViewHistorySelected(type) },
                    selected = viewHistory == type
                ) {
                    Text(label)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (viewHistory) {
                ViewHistory.DAILY -> {
                    DatePicker(
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected
                    )
                }

                ViewHistory.MONTHLY -> {
                    MonthPicker(
                        selectedMonth = selectedMonth,
                        onMonthSelected = onMonthSelected
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryTypeFilter(
    selectedTypes: List<History.Type>,
    onToggleType: (History.Type) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Type", // TODO: Use string resource if available
            style = MaterialTheme.typography.titleMedium
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            History.Type.entries.forEach { type ->
                FilterChip(
                    selected = selectedTypes.contains(type),
                    onClick = { onToggleType(type) },
                    label = { Text(stringResource(type.getLabelRes())) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = type.getBackgroundColorRes(),
                        selectedLabelColor = type.getOnBackgroundColorRes()
                    ),
                    shape = MaterialTheme.shapes.large
                )
            }
        }
    }
}

private fun History.Type.getLabelRes(): Int = when (this) {
    History.Type.CREDIT -> R.string.label_history_type_credit
    History.Type.DEBIT -> R.string.label_history_type_debit
    History.Type.TRANSFER -> R.string.label_history_type_transfer
}

@Composable
internal fun History.Type.getBackgroundColorRes(): Color = when (this) {
    History.Type.CREDIT -> ExpenseTrackerTheme.appColor.credit
    History.Type.DEBIT -> ExpenseTrackerTheme.appColor.debit
    History.Type.TRANSFER -> ExpenseTrackerTheme.appColor.transfer
}


@Composable
internal fun History.Type.getOnBackgroundColorRes(): Color = when (this) {
    History.Type.CREDIT -> ExpenseTrackerTheme.appColor.onCredit
    History.Type.DEBIT -> ExpenseTrackerTheme.appColor.onDebit
    History.Type.TRANSFER -> ExpenseTrackerTheme.appColor.onTransfer
}

@PreviewLightDark
@Composable
private fun HistoryFilterScreenPreview() {
    ExpenseTrackerTheme {
        HistoryFilterScreen(onFilterChanged = { _, _, _, _ -> })
    }
}
