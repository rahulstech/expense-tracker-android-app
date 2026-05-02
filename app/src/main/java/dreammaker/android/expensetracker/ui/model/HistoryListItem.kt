package dreammaker.android.expensetracker.ui.model

import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate

sealed class HistoryListItem {

    data class Header(
        val data: LocalDate
    ): HistoryListItem()

    data class Item(
        val data: History,
        var selected: Boolean = false
    ): HistoryListItem()

    class Placeholder(): HistoryListItem()
}