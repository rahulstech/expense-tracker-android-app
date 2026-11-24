package dreammaker.android.expensetracker.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.ui.LocalAppColor
import rahulstech.android.expensetracker.domain.model.HistoryType


@Immutable
data class HistoryTypeChooserItem(
    val type: HistoryType,
    val label: String,
    val selected: Boolean,
    val selectedContentColor: Color,
    val unselectedContentColor: Color,
    val selectedContainerColor: Color,
    val unselectedContainerColor: Color,
)

@Composable
fun HistoryTypeChooser(
    items: List<HistoryTypeChooserItem>,
    onSelect: (HistoryType)-> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            HistoryTypeChip(
                labelText = item.label,
                selected = item.selected,
                onClick = { onSelect(item.type) },
                selectedContainerColor = item.selectedContainerColor,
                selectedContentColor = item.selectedContentColor,
                unselectedContainerColor = item.unselectedContainerColor,
                unselectedContentColor = item.unselectedContentColor,
            )
        }
    }
}

@Composable
fun HistoryTypeChip(
    labelText: String,
    selected: Boolean,
    onClick: ()-> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labelStyle: TextStyle = MaterialTheme.typography.labelLarge,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    unselectedContainerColor: Color = Color.Transparent,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.primary,
) {
    FilterChip(
        modifier = modifier,
        enabled = enabled,
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                labelText,
                style = labelStyle,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        shape = MaterialTheme.shapes.extraLarge,
        colors = FilterChipDefaults.filterChipColors(
            labelColor = unselectedContentColor,
            selectedLabelColor = selectedContentColor,
            disabledLabelColor = if (selected) selectedContentColor.copy(alpha = .38f) else unselectedContentColor.copy(alpha = .38f),
            selectedContainerColor = selectedContainerColor,
            containerColor = unselectedContainerColor,
            disabledContainerColor = if (selected) selectedContainerColor.copy(alpha = .38f) else unselectedContainerColor.copy(alpha = .38f)
        ),
        border = BorderStroke(1.dp,selectedContainerColor)
    )
}


@PreviewLightDark
@Composable
fun HistoryTypeChooserPreview() {
    ExpenseTrackerTheme {
        Surface {
            HistoryTypeChooser(
                items = listOf(
                    HistoryTypeChooserItem(
                        type = HistoryType.CREDIT,
                        label = "Credit",
                        selected = false,
                        selectedContentColor = LocalAppColor.current.onCredit,
                        selectedContainerColor = LocalAppColor.current.credit,
                        unselectedContentColor = LocalAppColor.current.credit,
                        unselectedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    HistoryTypeChooserItem(
                        type = HistoryType.DEBIT,
                        label = "Debit",
                        selected = true,
                        selectedContentColor = LocalAppColor.current.onDebit,
                        selectedContainerColor = LocalAppColor.current.debit,
                        unselectedContentColor = LocalAppColor.current.debit,
                        unselectedContainerColor = MaterialTheme.colorScheme.surface,
                    )
                ),
                onSelect = {}
            )
        }
    }
}