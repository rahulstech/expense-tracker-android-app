package dreammaker.android.expensetracker.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.label_select_date),
    minDate: LocalDate = LocalDate.now().minusYears(30).withMonth(1).withDayOfMonth(1),
    maxDate: LocalDate = LocalDate.now().plusYears(20).withMonth(12).withDayOfMonth(31)
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropdownSelector(
                    label = selectedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    selectedItem = selectedDate.month,
                    items = Month.entries.toList(),
                    onItemSelected = { month ->
                        val newDate = try {
                            selectedDate.withMonth(month.value)
                        } catch (_: Exception) {
                            selectedDate.withMonth(month.value).withDayOfMonth(selectedDate.withMonth(month.value).lengthOfMonth())
                        }
                        onDateSelected(clampDate(newDate, minDate, maxDate))
                    },
                    itemLabel = { it.getDisplayName(TextStyle.FULL, Locale.getDefault()) }
                )

                Spacer(modifier = Modifier.width(8.dp))

                DropdownSelector(
                    label = selectedDate.year.toString(),
                    selectedItem = selectedDate.year,
                    items = (minDate.year..maxDate.year).toList(),
                    onItemSelected = { year ->
                        val newDate = try {
                            selectedDate.withYear(year)
                        } catch (_: Exception) {
                            selectedDate.withYear(year).withDayOfMonth(selectedDate.withYear(year).lengthOfMonth())
                        }
                        onDateSelected(clampDate(newDate, minDate, maxDate))
                    },
                    itemLabel = { it.toString() }
                )
            }
        }

        val daysInMonth = remember(selectedDate.month, selectedDate.year) {
            val firstDayOfMonth = selectedDate.withDayOfMonth(1)
            (0 until selectedDate.lengthOfMonth()).map { firstDayOfMonth.plusDays(it.toLong()) }
        }

        // Added a fixed height to LazyRow to support intrinsic measurements if the parent
        // requires them (fixes "intrinsic measurements of SubcomposeLayout" error).
        // DateChip height (60dp) + vertical content padding (4dp * 2) = 68dp.
        LazyRow(
            modifier = Modifier.height(68.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(daysInMonth) { date ->
                val isSelected = date == selectedDate
                DateChip(
                    date = date,
                    isSelected = isSelected,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
private fun <T> DropdownSelector(
    label: String,
    selectedItem: T,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val selectedIndex = remember(items, selectedItem) { items.indexOf(selectedItem) }

    LaunchedEffect(expanded, selectedItem) {
        if (expanded && selectedIndex >= 0) {
            // DropdownMenuItem guaranteed min-height = 48.dp
            val itemHeightPx = with(density) { 48.dp.toPx() }
            scrollState.scrollTo((selectedIndex * itemHeightPx).toInt())
        }
    }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { expanded = true }
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(if (expanded) 180f else 0f)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            scrollState = scrollState,
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemLabel(item),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun clampDate(date: LocalDate, min: LocalDate, max: LocalDate): LocalDate {
    return if (date.isBefore(min)) min else if (date.isAfter(max)) max else date
}

@Composable
private fun DateChip(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(48.dp)
            .height(60.dp)
            .clickable { onClick() }
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shape = MaterialTheme.shapes.medium
            )
            .border(
                border = if (isSelected) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                },
                shape = MaterialTheme.shapes.medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@PreviewLightDark
@Composable
fun DatePickerPreview() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    ExpenseTrackerTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            DatePicker(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                modifier = Modifier.padding(16.dp)
            )
        }

    }
}
