package dreammaker.android.expensetracker.ui.main

import androidx.core.view.MenuProvider
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

class ContextualActionBarViewModel: ViewModel() {

    private val _cabStartState = MutableStateFlow(false)
    val cabStartState: StateFlow<Boolean> get() = _cabStartState

    private val _cabTitleState = MutableStateFlow("")
    val cabTitleState: StateFlow<String> = _cabTitleState
    var cabTitle: String
        get() = _cabTitleState.value
        set(value) { _cabTitleState.value = value }

    private var _cabMenuRef: WeakReference<MenuProvider?> = WeakReference(null)
    val cabMenu: MenuProvider? get() = _cabMenuRef.get()

    fun startContextualActionBar(menu: MenuProvider? = null) {
        _cabMenuRef = WeakReference(menu)
        _cabStartState.value = true
    }

    fun endContextActionBar() {
        _cabStartState.value = false
    }
}