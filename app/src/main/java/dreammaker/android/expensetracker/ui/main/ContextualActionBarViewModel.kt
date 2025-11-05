package dreammaker.android.expensetracker.ui.main

import androidx.core.view.MenuProvider
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

class ContextualActionBarViewModel: ViewModel() {

    private val _cabStartState = MutableStateFlow<Boolean>(false)
    val cabStartState: StateFlow<Boolean> get() = _cabStartState

    private val _cabShowState = MutableStateFlow<Boolean>(false)
    val cabShowState: StateFlow<Boolean> get() = _cabShowState

    private val _cabTitle = MutableStateFlow<String>("")
    val cabTitle: StateFlow<String> get() = _cabTitle

    private var _cabMenuRef: WeakReference<MenuProvider?> = WeakReference(null)
    val cabMenu: MenuProvider? get() = _cabMenuRef.get()

    fun startContextualActionBar(menu: MenuProvider? = null) {
        _cabMenuRef = WeakReference(menu)
        _cabShowState.value = true
        _cabStartState.value = true
    }

    fun showContextActionBar() {
        _cabShowState.value = true
    }

    fun hideContextActionBar() {
        _cabShowState.value = false
    }

    fun endContextActionBar() {
        _cabShowState.value = false
        _cabMenuRef = WeakReference(null)
        _cabStartState.value = false
    }

    fun updateTitle(title: String) {
        _cabTitle.value = title
    }
}