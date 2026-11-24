package dreammaker.android.expensetracker.ui.history.historyinput

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import dreammaker.android.expensetracker.ui.navigation.AppDestination
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import rahulstech.android.expensetracker.domain.model.HistoryType
import java.time.LocalDate

sealed interface HistoryInputMode {

    companion object {
        fun fromRoute(route: AppDestination): HistoryInputMode =
            when(route) {
                is AppDestination.AddCreditDebit -> Add(route.accountId, route.groupId)
                is AppDestination.EditCreditDebit -> Edit(route.historyId)
                is AppDestination.AddTransfer -> Add(route.sourceAccountId)
                is AppDestination.EditTransfer -> Edit(route.historyId)
                is AppDestination.AddIncomeExpense -> Add(route.accountId)
                is AppDestination.EditIncomeExpense -> Edit(route.historyId)
                else -> {
                    throw IllegalStateException("can not build HistoryInputMode from ${route.javaClass.simpleName}")
                }
            }
    }

    data class Add(
        val accountId: Long? = null,
        val groupId: Long? = null
    ): HistoryInputMode

    data class Edit(val historyId: Long): HistoryInputMode
}


sealed interface HistoryInputDialog {

    data object None: HistoryInputDialog

    data object PendingSave: HistoryInputDialog

    data class SaveError(val cause: Throwable): HistoryInputDialog

    data object AddMore: HistoryInputDialog
}

@Immutable
data class HistoryInputUiState(
    val type: HistoryType,
    val id: Long = 0,
    val date: LocalDate = LocalDate.now(),
    val amount: String = "",
    val sourceAccount: Account? = null,
    val destinationAccount: Account? = null,
    val group: Group? = null,
    val note: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    @param:StringRes val amountError: Int? = null,
    @param:StringRes val sourceAccountError: Int? = null,
    @param:StringRes val destinationAccountError: Int? = null,
    val dialog: HistoryInputDialog = HistoryInputDialog.None,

    val isAccountLoading: Boolean = false,
    val isGroupLoading: Boolean = false,
    val recentAccounts: List<Account> = emptyList(),
    val allAccounts: List<Account> = emptyList(),
    val recentGroups: List<Group> = emptyList(),
    val allGroups: List<Group> = emptyList()
) {

    fun resetInputErrors(): HistoryInputUiState = this.copy(
        amountError = null,
        sourceAccountError = null,
        destinationAccountError = null
    )

    fun reset(): HistoryInputUiState = this.copy(
        isLoading = false,
        isSaving = false,
        id = 0,
        amount = "",

//      date, accounts, group are not reset to keep as last choice
//        date = LocalDate.now()
//        sourceAccount = null,
//        destinationAccount = null,
//        group = null,

        note = "",
        amountError = null,
        sourceAccountError = null,
        destinationAccountError = null,
        dialog = HistoryInputDialog.None
    )

    fun toHistory(): History {
        val amount = amount.toBigDecimal()
        val primaryAccountId = sourceAccount?.id
        val secondaryAccountId = destinationAccount?.id
        val groupId = group?.id
        val note = note.takeIf { it.isNotBlank() }

        return when(type) {
            HistoryType.CREDIT -> {
                History.CreditHistory(
                    id, date, amount, primaryAccountId!!, groupId, note
                )
            }

            HistoryType.DEBIT -> {
                History.DebitHistory(id, date, amount, primaryAccountId!!, groupId, note)
            }

            HistoryType.CREDIT_INCOME -> {
                History.CreditIncomeHistory(
                    id, date, amount, primaryAccountId!!, note
                )
            }

            HistoryType.DEBIT_EXPENSE -> {
                History.DebitExpenseHistory(
                    id, date, amount, primaryAccountId!!, note
                )
            }

            HistoryType.TRANSFER -> {
                History.TransferHistory(
                    id, date, amount, primaryAccountId!!, secondaryAccountId!!, note
                )
            }
        }
    }
}


@Immutable
data class AccountState(
    val isLoading: Boolean = false,
    val allAccounts: List<Account> = emptyList(),
    val recentAccounts: List<Account> = emptyList()
)

@Immutable
data class GroupState(
    val isLoading: Boolean = false,
    val allGroups: List<Group> = emptyList(),
    val recentGroups: List<Group> = emptyList()
)

sealed interface HistoryInputUiEvent {

    data class DateChange(val date: LocalDate): HistoryInputUiEvent

    data class HistoryTypeChange(val type: HistoryType): HistoryInputUiEvent

    data class AmountChange(val amount: String): HistoryInputUiEvent

    data class NoteChange(val note: String): HistoryInputUiEvent

    data class SourceAccountChange(val account: Account?): HistoryInputUiEvent

    data class DestinationAccountChange(val account: Account?): HistoryInputUiEvent

    data class GroupChange(val group: Group?): HistoryInputUiEvent

    data object Save: HistoryInputUiEvent

    data object AddMore: HistoryInputUiEvent

    data object DismissDialog: HistoryInputUiEvent
}


sealed interface HistoryInputUiEffect {

    data class ShowLoadErrorAndExit(val cause: Throwable): HistoryInputUiEffect

    data object ShowNotFoundAndExit: HistoryInputUiEffect

    data class ShowSaveSuccessful(val exit: Boolean): HistoryInputUiEffect
}

data class ValidationErrorResult(
    @param:StringRes val amountError: Int? = null,
    @param:StringRes val sourceAccountError: Int? = null,
    @param:StringRes val destinationAccountError: Int? = null,
)