package dreammaker.android.expensetracker.ui.history.historyinput.picker.group

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.application
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.ui.GroupListItem
import dreammaker.android.expensetracker.util.insertSeparator
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import rahulstech.android.expensetracker.domain.ExpenseRepository
import kotlin.time.Duration.Companion.milliseconds

class GroupPickerViewModel(
    app: Application
): AndroidViewModel(app) {

    private val groupRepo = ExpenseRepository.getInstance(app).groupRepository

    private val searchTextState = MutableStateFlow<String?>(null)
    private lateinit var allGroups: LiveData<List<GroupListItem>>

    var searchText: String?
        get() = searchTextState.value
        set(value) {
            searchTextState.value = value
        }

    val groupListItems: List<GroupListItem> get() = allGroups.value ?: emptyList()

    @OptIn(FlowPreview::class)
    fun getAllGroups(maxFrequentlyUsed: Int = Constants.DEFAULT_MAX_FREQUENTLY_USED_ITEM): LiveData<List<GroupListItem>> {
        if (!::allGroups.isInitialized) {
            allGroups = searchTextState
                .debounce(150.milliseconds)
                .asLiveData().switchMap { searchText ->
                    groupRepo.getLiveAllGroups().map { groups ->
                        val filteredGroups = if (searchText.isNullOrBlank()) groups
                        else {
                            groups.filter { group ->
                                group.name.contains(searchText, true)
                            }
                        }
                        filteredGroups
                            .sortedByDescending { it.totalUsed }
                            .mapIndexed{ index, group ->
                                GroupListItem.Item(
                                    data = group,
                                    rank = if(group.isUsedAfterCreate()) index+1 else 0 // frequently used rank
                                )
                            }
                            .insertSeparator { before, after ->
                                if (null != after) {
                                    if (null == before) {
                                        return@insertSeparator GroupListItem.Header(
                                            when(after.rank) {
                                                0 -> application.getString(R.string.label_header_others)
                                                else -> application.getString(R.string.label_header_frequently_used)
                                            }
                                        )
                                    }

                                    // if previous rank = 0 means others headers is already added
                                    // rank of last item is max means allowed number of items added under frequently used,
                                    // no matter what is the next rank just add others headers
                                    val startOthers = (before.rank > 0 && before.rank < maxFrequentlyUsed && after.rank == 0) || (before.rank == maxFrequentlyUsed)
                                    if (startOthers) {
                                        return@insertSeparator GroupListItem.Header(application.getString(R.string.label_header_others))
                                    }
                                }
                                null
                            }
                    }
                }
        }
        return allGroups
    }
}
