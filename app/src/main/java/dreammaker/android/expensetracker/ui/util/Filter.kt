package dreammaker.android.expensetracker.ui.util;

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

class Filter<FilterData, ResultType>(private val filterCallback: FilterCallback<FilterData,ResultType>) {
    private val TAG = Filter::class.simpleName

    fun interface FilterCallback<FilterData,ResultType> {
        fun doFilter(data: FilterData?, original: ResultType?): ResultType
    }

    private val filterArgs by lazy { MutableStateFlow<Pair<FilterData?,ResultType?>>((null to null)) }
    private val _resultLiveData: MutableLiveData<ResultType> = MutableLiveData()
    val resultLiveData: LiveData<ResultType> = _resultLiveData

    suspend fun start() {
        filterArgs.collectLatest { args ->
            val data = args.first
            val original = args.second
            performFilter(data,original)
        }
    }

   fun filter(data: FilterData?, original: ResultType?) {
       filterArgs.value = (data to original)
    }

    private suspend fun performFilter(data: FilterData?, original: ResultType?) {
        withContext(Dispatchers.IO) {
            ensureActive()
            val result = filterCallback.doFilter(data, original)
            ensureActive()
            submitResult(result)
        }
    }

    private fun submitResult(result: ResultType) {
        _resultLiveData.postValue(result)
    }
}