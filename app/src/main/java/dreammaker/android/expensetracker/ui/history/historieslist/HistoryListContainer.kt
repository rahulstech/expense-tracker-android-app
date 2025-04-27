package dreammaker.android.expensetracker.ui.history.historieslist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.settings.SettingsProvider
import dreammaker.android.expensetracker.settings.ViewHistory
import dreammaker.android.expensetracker.ui.history.historieslist.daily.DailyViewHistoryFragment

class HistoryListContainer: Fragment(), MenuProvider {

    companion object {
        private val TAG = HistoryListContainer::class.simpleName
        private const val TAG_DAILY_HISTORY = "daily_history"
        private const val TAG_MONTHLY_HISTORY = "monthly_history"
        private const val CONTAINER_ID = 1
    }

    private var _container: FragmentContainerView? = null
    private val container get() = _container!!

    private lateinit var navController: NavController
    private lateinit var settings: SettingsProvider

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settings = SettingsProvider.get(requireContext())
        _container = FragmentContainerView(requireContext()).apply {
            id = CONTAINER_ID
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        return container
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)

        val fragmentManager = childFragmentManager
        val transaction = childFragmentManager.beginTransaction()
        var fragment = fragmentManager.findFragmentByTag(TAG_DAILY_HISTORY)
        if (null == fragment) {
            fragment = DailyViewHistoryFragment()
            transaction.add(CONTAINER_ID, fragment, TAG_DAILY_HISTORY)
        }
        else {
            transaction.show(fragment)
        }
        transaction.commit()

        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.history_list_menu, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        val viewAs = settings.getViewHistory()
        val itemDaily = menu.findItem(R.id.menu_view_as_daily)
        val itemMonthly = menu.findItem(R.id.menu_view_as_monthly)
        when(viewAs) {
            ViewHistory.MONTHLY -> itemMonthly.isChecked = true
            ViewHistory.DAILY -> itemDaily.isChecked = true
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_view_as_daily -> {
                changeViewHistory(ViewHistory.DAILY)
                true
            }
            R.id.menu_view_as_monthly -> {
                changeViewHistory(ViewHistory.MONTHLY)
                true
            }
            else -> false
        }
    }

    private fun changeViewHistory(viewAs: ViewHistory) {
        Log.i(TAG,"changeViewHistory viewAs=$viewAs")
        settings.setViewHistory(viewAs)
        requireActivity().invalidateOptionsMenu()
    }
}