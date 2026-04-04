package dreammaker.android.expensetracker.ui.group.groupslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Group
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupRepo: GroupRepository
): ViewModel() {
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