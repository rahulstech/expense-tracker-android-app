package dreammaker.android.expensetracker.ui.group.inputgroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.GroupRepository
import rahulstech.android.expensetracker.domain.model.Group
import javax.inject.Inject

data class GroupInputUIState(
    val isLoadingGroup: Boolean = false,
    val group: Group? = null,
    val groupLoadError: Throwable? = null,
    val isSaving: Boolean = false,
    val isSaveSuccessful: Boolean = false,
    val saveError: Throwable? = null
)

@HiltViewModel
class GroupInputViewModel @Inject constructor(
    private val groupRepo: GroupRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(GroupInputUIState())
    val uiState: StateFlow<GroupInputUIState> = _uiState.asStateFlow()

    private val currentUIState: GroupInputUIState get() = _uiState.value

    private var lastFoundGroupId = 0L

    fun findGroupById(id: Long) {
        if (id == lastFoundGroupId) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            updateLoadState(isLoadingGroup = true)
            try {
                val group = groupRepo.findGroupById(id)
                updateLoadState(isLoadingGroup = false, group = group)
                lastFoundGroupId = id
            } catch (th: Throwable) {
                updateLoadState(isLoadingGroup = false, group = null, groupLoadError = th)
            }
        }
    }

    fun saveGroup(group: Group, isEdit: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            updateSaveState(isSaving = true)
            try {
                if (isEdit) {
                    groupRepo.updateGroup(group)
                } else {
                    groupRepo.insertGroup(group)
                }
                updateSaveState(isSaving = false, isSaveSuccessful = true)
            } catch (th: Throwable) {
                updateSaveState(isSaving = false, isSaveSuccessful = false, saveError = th)
            }
        }
    }

    private fun updateLoadState(
        isLoadingGroup: Boolean = currentUIState.isLoadingGroup,
        group: Group? = currentUIState.group,
        groupLoadError: Throwable? = currentUIState.groupLoadError,
    ) {
        _uiState.update {
            it.copy(
                isLoadingGroup = isLoadingGroup,
                group = group,
                groupLoadError = groupLoadError
            )
        }
    }

    private fun updateSaveState(
        isSaving: Boolean = currentUIState.isSaving,
        isSaveSuccessful: Boolean = currentUIState.isSaveSuccessful,
        saveError: Throwable? = currentUIState.saveError
    ) {
        _uiState.update {
            it.copy(
                isSaving = isSaving,
                isSaveSuccessful = isSaveSuccessful,
                saveError = saveError
            )
        }
    }
}
