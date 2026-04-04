package dreammaker.android.expensetracker.ui.history.historyinput.picker.account

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dreammaker.android.expensetracker.Constants.DEFAULT_MAX_FREQUENTLY_USED_ITEM
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.ui.AccountListItem
import dreammaker.android.expensetracker.util.insertSeparator
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import rahulstech.android.expensetracker.domain.AccountRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AccountPickerViewModel @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context,
    private val accountRepo: AccountRepository
): ViewModel() {
    private val searchTextState = MutableStateFlow<String?>(null)
    private var _accountListItems: LiveData<List<AccountListItem>>? = null

    var searchText: String?
        get() = searchTextState.value
        set(value) {
            searchTextState.value = value
        }

    val accountListItems: List<AccountListItem> get() = _accountListItems?.value ?: emptyList()

    @OptIn(FlowPreview::class)
    fun getAllAccounts(maxFrequentlyUsed: Int = DEFAULT_MAX_FREQUENTLY_USED_ITEM): LiveData<List<AccountListItem>> {
        if (null == _accountListItems) {
            _accountListItems = searchTextState
                .debounce(150.milliseconds)
                .asLiveData().switchMap { searchText ->
                accountRepo.getLiveAllAccounts().map { accounts ->
                    val filteredAccounts = if (searchText.isNullOrBlank()) {
                        accounts
                    } else {
                        accounts.filter { account -> account.name.contains(searchText, true) }
                    }
                    filteredAccounts.sortedByDescending { it.totalUsed }
                        .mapIndexed { index, account ->
                            AccountListItem.Item(
                                data = account,
                                rank = if (account.isUsedAfterCreate()) index + 1 else 0
                            )
                        }
                        .insertSeparator { before, after ->
                            if (null != after) {
                                if (null == before) {
                                    return@insertSeparator AccountListItem.Header(
                                        when (after.rank) {
                                            0 -> applicationContext.getString(R.string.label_header_others)
                                            else -> applicationContext.getString(R.string.label_header_frequently_used)
                                        }
                                    )
                                }

                                val startOthers =
                                    (before.rank > 0 && before.rank < maxFrequentlyUsed && after.rank == 0) || (before.rank == maxFrequentlyUsed)
                                if (startOthers) {
                                    return@insertSeparator AccountListItem.Header(
                                        applicationContext.getString(
                                            R.string.label_header_others
                                        )
                                    )
                                }
                            }
                            null
                        }
                }
            }
        }
        return _accountListItems!!
    }
}
