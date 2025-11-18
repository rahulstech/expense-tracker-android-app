package dreammaker.android.expensetracker.ui.history.historyinput.picker.account

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import rahulstech.android.expensetracker.domain.ExpenseRepository
import rahulstech.android.expensetracker.domain.model.Account

class AccountPickerViewModel(
    app: Application
): ViewModel() {

    private val accountRepo = ExpenseRepository.getInstance(app).accountRepository

    private lateinit var allAccount: LiveData<List<Account>>

    fun getAllAccounts(): LiveData<List<Account>> {
        if (!::allAccount.isInitialized) {
            allAccount = accountRepo.getLiveAllAccounts()
        }
        return allAccount
    }
}
