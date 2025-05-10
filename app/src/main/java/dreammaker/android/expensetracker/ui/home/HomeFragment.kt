package dreammaker.android.expensetracker.ui.home

import android.graphics.Typeface
import android.os.Bundle
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.GroupModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.databinding.HomeBinding
import dreammaker.android.expensetracker.databinding.RecentItemViewBinding
import dreammaker.android.expensetracker.ui.history.historyinput.HistoryInputFragment
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.getBalanceText
import dreammaker.android.expensetracker.ui.util.isVisible
import dreammaker.android.expensetracker.ui.util.putHistoryType
import dreammaker.android.expensetracker.ui.util.toCurrencyString
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible

class HomeFragment: Fragment()  {
    private val TAG = HomeFragment::class.simpleName

    private var _binding: HomeBinding? = null
    private val binding get() = _binding!!

    private val navController: NavController by lazy { findNavController() }
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prepareAccountsPlaceholder()
        prepareGroupsPlaceholder()
        binding.btnViewAllAccounts.setOnClickListener {
            navController.navigate(R.id.action_home_to_accounts_list)
        }
        binding.btnViewAllGroups.setOnClickListener {
            navController.navigate(R.id.action_home_to_groups_list)
        }
        binding.addHistory.setOnClickListener {
            val target = binding.buttonsLayout
            if (target.isVisible()) {
                target.visibilityGone()
            }
            else {
                target.visible()
            }
        }
        binding.btnAddCredit.setOnClickListener { navigateToCreateHistory(HistoryType.CREDIT) }
        binding.btnAddDebit.setOnClickListener { navigateToCreateHistory(HistoryType.DEBIT) }

        viewModel.getTotalBalance().observe(viewLifecycleOwner, this::onTotalBalanceLoaded)
        viewModel.getRecentlyUsedThreeAccounts().observe(viewLifecycleOwner, this::onRecentlyUsedAccountsLoaded)
        viewModel.getRecentlyUsedThreeGroups().observe(viewLifecycleOwner, this::onRecentlyUsedGroupsLoaded)
    }

    private fun prepareAccountsPlaceholder() {
        binding.emptyAccountsPlaceholder.text = makeStyledPlaceholderText(getString(R.string.label_empty_recent_account_list),
            getString(R.string.description_empty_recent_account_list))
    }

    private fun prepareGroupsPlaceholder() {
        binding.emptyGroupsPlaceholder.text = makeStyledPlaceholderText(getString(R.string.label_empty_recent_group_list),
            getString(R.string.description_empty_recent_group_list))
    }

    private fun makeStyledPlaceholderText(label: String, description: String): CharSequence {
        return buildSpannedString {
            inSpans(RelativeSizeSpan(1.25f), StyleSpan(Typeface.BOLD_ITALIC)) { append(label) }
            append("\n")
            inSpans(RelativeSizeSpan(.85f)) { append(description) }
        }
    }

    private fun navigateToCreateHistory(type: HistoryType) {
        navController.navigate(R.id.action_home_to_create_history, Bundle().apply {
            putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
            putHistoryType(HistoryInputFragment.ARG_HISTORY_TYPE, type)
        })
    }

    private fun onTotalBalanceLoaded(totalBalance: Double?) {
        if (null == totalBalance) {
            binding.totalBalance.text = 0f.toCurrencyString()
        }
        else {
            binding.totalBalance.text = totalBalance.toCurrencyString()
        }
    }

    private fun onRecentlyUsedAccountsLoaded(accounts: List<AccountModel>) {
        addRecentlyUsedViews(binding.accounts, binding.emptyAccountsPlaceholder, accounts) { container, account,_ ->
            val binding = RecentItemViewBinding.inflate(layoutInflater, container, false)
            binding.apply {
                icon.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_account_black, requireContext().theme))
                text1.text = account.name
                text2.text = account.getBalanceText(requireContext()) // TODO: apply countryCode and locale
                root.setOnClickListener {
                    navController.navigate(R.id.action_home_to_view_account, Bundle().apply {
                        putLong(Constants.ARG_ID, account.id!!)
                    })
                }
            }
            binding.root
        }
    }

    private fun onRecentlyUsedGroupsLoaded(groups: List<GroupModel>) {
        addRecentlyUsedViews(binding.groups, binding.emptyGroupsPlaceholder, groups) { container,group,_ ->
            val binding = RecentItemViewBinding.inflate(layoutInflater, container, false)
            binding.apply {
                icon.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_group_64, requireContext().theme))
                text1.text = group.name
                text2.text = group.getBalanceText(requireContext()) // TODO: apply countryCode and locale
                root.setOnClickListener {
                    navController.navigate(R.id.action_home_to_view_group, Bundle().apply {
                        putLong(Constants.ARG_ID, group.id!!)
                    })
                }
            }
            binding.root
        }
    }

    private fun <T> addRecentlyUsedViews(container: ViewGroup, placeholder: View, data: List<T>, factory: (ViewGroup, T, Int)->View) {
        container.removeAllViews()
        if (data.isEmpty()) {
            container.visibilityGone()
            placeholder.visible()
        }
        else {
            data.forEachIndexed { index, item ->
                val view = factory(container,item, index)
                container.addView(view)
            }
            placeholder.visibilityGone()
            container.visible()
        }

    }
}