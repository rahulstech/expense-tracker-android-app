package dreammaker.android.expensetracker.ui.history.historieslist

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.BundleCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.settings.SettingsProvider
import dreammaker.android.expensetracker.settings.ViewHistory
import dreammaker.android.expensetracker.ui.history.historieslist.daily.DailyViewHistoryFragment
import dreammaker.android.expensetracker.ui.history.historieslist.monthly.MonthlyViewHistoryFragment
import dreammaker.android.expensetracker.util.AccountModelParcel
import dreammaker.android.expensetracker.util.GroupModelParcel
import dreammaker.android.expensetracker.util.setActivitySubTitle

class HistoryListContainer: Fragment(), MenuProvider {

    companion object {
        private val TAG = HistoryListContainer::class.simpleName
        private const val TAG_DAILY_HISTORY = "daily_history"
        private const val TAG_MONTHLY_HISTORY = "monthly_history"
        private const val CONTAINER_ID = 1

        const val ARG_SHOW_HISTORY_FOR = "arg_show_history_for"
    }

    private var _container: FragmentContainerView? = null
    private val container get() = _container!!

    private val navController: NavController by lazy { findNavController() }
    private lateinit var settings: SettingsProvider

    private var showHistoryFor: Parcelable? = null

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
        showHistoryFor =  arguments?.let { BundleCompat.getParcelable(it, ARG_SHOW_HISTORY_FOR, Parcelable::class.java) }

        navController.currentBackStackEntry?.savedStateHandle?.set(ARG_SHOW_HISTORY_FOR, showHistoryFor)
        val viewAs = settings.getViewHistory()
        changeFragment(viewAs)
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.history_list_menu, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        val viewAs = settings.getViewHistory()
        when(viewAs) {
            ViewHistory.MONTHLY -> menu.findItem(R.id.menu_view_as_monthly).setChecked(true)
            ViewHistory.DAILY -> menu.findItem(R.id.menu_view_as_daily).setChecked(true)
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

    override fun onResume() {
        super.onResume()
        showHistoryFor?.let { entity ->
            when(entity) {
                is AccountModelParcel -> setActivitySubTitle(entity.name)
                is GroupModelParcel -> setActivitySubTitle(entity.name)
            }
        }
    }

    override fun onPause() {
        setActivitySubTitle("")
        super.onPause()
    }

    private fun changeViewHistory(viewAs: ViewHistory) {
        settings.setViewHistory(viewAs)
        requireActivity().invalidateOptionsMenu()
        changeFragment(viewAs)
    }

    private fun changeFragment(viewAs: ViewHistory) {
        val tag = when(viewAs) {
            ViewHistory.MONTHLY -> TAG_MONTHLY_HISTORY
            ViewHistory.DAILY -> TAG_DAILY_HISTORY
        }
        val fragmentManager = childFragmentManager
        val transaction = childFragmentManager.beginTransaction()
        fragmentManager.fragments.forEach { transaction.hide(it) }
        var fragment = fragmentManager.findFragmentByTag(tag)
        if (null == fragment) {
            fragment = createFragment(tag)
            transaction.add(CONTAINER_ID, fragment, tag)
        }
        else {
            transaction.show(fragment)
        }
        transaction.commit()
    }

    private fun createFragment(tag: String): Fragment {
        return when(tag) {
            TAG_MONTHLY_HISTORY -> MonthlyViewHistoryFragment()
            TAG_DAILY_HISTORY -> DailyViewHistoryFragment()
            else -> throw IllegalArgumentException("unknown tag '$tag'")
        }
    }
}