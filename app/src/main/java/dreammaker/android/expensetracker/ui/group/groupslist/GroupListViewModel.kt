package dreammaker.android.expensetracker.ui.group.groupslist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.ui.UIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.model.Group
import kotlin.time.Duration.Companion.milliseconds

class GroupListViewModel(
    app: Application
): AndroidViewModel(app) {

    private val groupRepo = ExpenseRepository.getInstance(app).groupRepository

    private val searchTextState = MutableStateFlow<String?>(null)
    private var _groups: LiveData<List<Group>>? = null

    var searchText: String?
        get() = searchTextState.value
        set(value) {
            searchTextState.value = value
        }

    @OptIn(FlowPreview::class)
    fun getAllGroups(): LiveData<List<Group>> {
        if (null == _groups) {
            _groups = searchTextState
                .debounce(150.milliseconds)
                .asLiveData()
                .switchMap { searchText ->
                    groupRepo.getLiveAllGroups().map { groups ->
                        if (searchText.isNullOrBlank()) {
                            groups
                        }
                        else {
                            groups.filter { group -> group.name.contains(searchText,true) }
                        }
                    }
                }
        }
        return _groups!!
    }

    private val _deleteGroupsState = MutableSharedFlow<UIState<Nothing>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val deleteGroupsState: Flow<UIState<Nothing>?> get() = _deleteGroupsState


    fun deleteGroups(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                _deleteGroupsState.tryEmit(UIState.UILoading())
                groupRepo.deleteMultipleGroups(ids)
                emit(null)
            }
                .catch { error -> _deleteGroupsState.tryEmit(UIState.UIError(error)) }
                .collect { _deleteGroupsState.tryEmit(UIState.UISuccess()) }
        }
    }
}