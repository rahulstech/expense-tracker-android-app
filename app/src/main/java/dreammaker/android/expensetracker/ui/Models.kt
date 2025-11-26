package dreammaker.android.expensetracker.ui

import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate

sealed class AccountListItem {

    data class Header(val data: CharSequence): AccountListItem()

    data class Item(
        val data: Account,
        var rank: Int = 0,
        var selected: Boolean = false,
    ): AccountListItem()
}

sealed class HistoryListItem {

    data class Header(
        val date: LocalDate
    ): HistoryListItem()

    data class Item(
        val history: History,
        var selected: Boolean = false
    ): HistoryListItem()

    class Placeholder(): HistoryListItem()
}

sealed class GroupListItem {

    data class Header(val data: CharSequence): GroupListItem()

    data class Item(
        val data: Group,
        var rank: Int = 0,
        var selected: Boolean = false,
    ): GroupListItem()
}

enum class HistoryType {
    CREDIT,
    DEBIT,
    TRANSFER,
    ;
}