package dreammaker.android.expensetracker.ui.history.historieslist

import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType

class HistoryFilterData {

    val types = mutableListOf<HistoryType>()

    fun setTypes(type: List<HistoryType>) {
        types.clear()
        types.addAll(type)
    }

    override fun toString(): String {
        return "HistoryFilterData(types=$types)"
    }
}

fun doFilterHistory(query: HistoryFilterData?, histories: List<HistoryModel>?): List<HistoryModel> {
    var result = histories ?: emptyList()
    query?.let {
        val types = query.types
        result = result.filter { history -> types.contains(history.type) }
        return@doFilterHistory result
    }
    return result
}

