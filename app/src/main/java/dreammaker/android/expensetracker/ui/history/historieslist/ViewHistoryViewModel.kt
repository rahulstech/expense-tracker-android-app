package dreammaker.android.expensetracker.ui.history.historieslist

import android.app.Application
import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.Date
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.HistoryDao
import dreammaker.android.expensetracker.database.HistoryModel
import dreammaker.android.expensetracker.database.HistoryType
import dreammaker.android.expensetracker.ui.util.toCurrencyString
import dreammaker.android.expensetracker.util.MonthYear
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class HistorySummary(
    val totalCredit: Float = 0f,
    val totalDebit: Float = 0f
) {

    fun getTotalCreditText(
        context: Context,
        currencyCode: String = "USD",
        locale: Locale = Locale.ENGLISH
    ): CharSequence {
        return buildLabeledColoredAmountText(
            context = context,
            labelResId = R.string.label_total_credit,
            amount = totalCredit,
            colorRes = R.color.colorCredit,
            currencyCode = currencyCode,
            locale = locale
        )
    }

    fun getTotalDebitText(
        context: Context,
        currencyCode: String = "USD",
        locale: Locale = Locale.ENGLISH
    ): CharSequence {
        return buildLabeledColoredAmountText(
            context = context,
            labelResId = R.string.label_total_debit,
            amount = totalDebit,
            colorRes = R.color.colorDebit,
            currencyCode = currencyCode,
            locale = locale
        )
    }

    private fun buildLabeledColoredAmountText(
        context: Context,
        @StringRes labelResId: Int,
        amount: Float,
        @ColorRes colorRes: Int,
        currencyCode: String,
        locale: Locale
    ): CharSequence {
        val color = ResourcesCompat.getColor(context.resources, colorRes, context.theme)
        return buildSpannedString {
            append(context.getString(labelResId))
            append(" ")
            color(color) {
                append(amount.toCurrencyString(currencyCode, locale))
            }
        }
    }
}


class HistoriesLiveDataFactory private constructor(val start: Date, val end: Date){

    private var accountId: Long? = null

    private var groupId: Long? = null

    companion object {
        fun forMonthYear(monthYear: MonthYear) = HistoriesLiveDataFactory(monthYear.toFirstDate(), monthYear.toLastDate())

        fun forDate(date: Date) = HistoriesLiveDataFactory(date, date)
    }

    fun ofAccount(id: Long): HistoriesLiveDataFactory {
        accountId = id
        return this
    }

    fun ofGroup(id: Long): HistoriesLiveDataFactory {
        groupId = id
        return this
    }

    fun createLiveData(dao: HistoryDao): LiveData<List<HistoryModel>> {
        if (null != accountId) {
            return dao.getHistoriesBetweenDatesOnlyForAccount(start,end, accountId!!)
        }
        else if (null != groupId) {
            return dao.getHistoriesBetweenDatesOnlyForGroup(start,end, groupId!!)
        }
        else {
            return dao.getHistoriesBetweenDates(start,end)
        }
    }

    override fun toString(): String {
        return "HistoriesLiveDataFactory{start=$start, end=$end, accountId=$accountId, groupId=$groupId}"
    }
}

class ViewHistoryViewModel(app: Application): AndroidViewModel(app) {

    private val TAG = ViewHistoryViewModel::class.simpleName

    private val historiesDao: HistoryDao

    init {
        val db = ExpensesDatabase.getInstance(app)
        historiesDao = db.historyDao
    }

    private lateinit var historiesLiveData: LiveData<List<HistoryModel>>

    private fun getOrCreateHistoriesLiveData(factory: HistoriesLiveDataFactory): LiveData<List<HistoryModel>> {
        if (!::historiesLiveData.isInitialized) {
            historiesLiveData = factory.createLiveData(historiesDao)
        }
        return historiesLiveData
    }

    fun getHistories(): List<HistoryModel>? {
        if (!::historiesLiveData.isInitialized) {
            return null
        }
        return historiesLiveData.value
    }

    fun getMonthlyHistories(monthYear: MonthYear): LiveData<List<HistoryModel>>
    = getOrCreateHistoriesLiveData(HistoriesLiveDataFactory.forMonthYear(monthYear))

    fun getMonthlyHistoriesForAccount(monthYear: MonthYear, accountId: Long): LiveData<List<HistoryModel>>
    = getOrCreateHistoriesLiveData(HistoriesLiveDataFactory.forMonthYear(monthYear).ofAccount(accountId))

    fun getMonthlyHistoriesForGroup(monthYear: MonthYear, groupId: Long): LiveData<List<HistoryModel>>
    = getOrCreateHistoriesLiveData(HistoriesLiveDataFactory.forMonthYear(monthYear).ofGroup(groupId))

    fun getDailyHistories(date: Date): LiveData<List<HistoryModel>>
    = getOrCreateHistoriesLiveData(HistoriesLiveDataFactory.forDate(date))

    fun getDailyHistoriesForAccount(date: Date, accountId: Long): LiveData<List<HistoryModel>>
    = getOrCreateHistoriesLiveData(HistoriesLiveDataFactory.forDate(date).ofAccount(accountId))

    fun getDailyHistoriesForGroup(date: Date, groupId: Long): LiveData<List<HistoryModel>>
    = getOrCreateHistoriesLiveData(HistoriesLiveDataFactory.forDate(date).ofGroup(groupId))

    private val _summaryLiveData = MutableLiveData<HistorySummary>().apply { value = HistorySummary() }
    private val historiesFlow = MutableStateFlow<List<HistoryModel>>(emptyList())
    val historySummary: LiveData<HistorySummary> get() = _summaryLiveData
    init {
        viewModelScope.launch {
            historiesFlow.collectLatest { histories ->
                val summery = withContext(Dispatchers.Default) {
                    var totalCredit = 0f
                    var totalDebit = 0f
                    histories.forEach { history ->
                        val amount = history.amount ?: return@forEach
                        when(history.type) {
                            HistoryType.CREDIT -> {
                                totalCredit += amount
                            }
                            HistoryType.DEBIT -> {
                                totalDebit += amount
                            }
                            else -> {}
                        }
                    }
                    HistorySummary(totalCredit,totalDebit)
                }
                _summaryLiveData.postValue(summery)
            }
        }
    }

    fun obtainSummary(histories: List<HistoryModel>?) {
        viewModelScope.launch { historiesFlow.emit(histories ?: emptyList()) }
    }
}