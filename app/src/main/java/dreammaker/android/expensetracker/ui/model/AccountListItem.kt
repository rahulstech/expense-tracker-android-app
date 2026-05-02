package dreammaker.android.expensetracker.ui.model

import rahulstech.android.expensetracker.domain.model.Account

sealed class AccountListItem {

    data class Header(val data: CharSequence): AccountListItem()

    data class Item(
        val data: Account,
        var rank: Int = 0,
        var selected: Boolean = false,
        var enabled: Boolean = true,
    ): AccountListItem()
}