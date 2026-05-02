package dreammaker.android.expensetracker.ui.model

import rahulstech.android.expensetracker.domain.model.Group

sealed class GroupListItem {

    data class Header(val data: CharSequence): GroupListItem()

    data class Item(
        val data: Group,
        var rank: Int = 0,
        var selected: Boolean = false,
    ): GroupListItem()
}