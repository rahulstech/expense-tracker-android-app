package dreammaker.android.expensetracker.ui.history.historieslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import dreammaker.android.expensetracker.ui.HistoryListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import rahulstech.android.expensetracker.domain.HistoryFilterParameters
import rahulstech.android.expensetracker.domain.HistoryRepository
import rahulstech.android.expensetracker.domain.model.History
import rahulstech.android.expensetracker.domain.model.HistoryTotalCreditTotalDebit
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ViewHistoryViewModelTest {

    val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getHistories() = runTest {

        val histories = listOf(
            History.CreditHistory(
                id = 1L,
                date = LocalDate.of(2026, 1, 1),
                amount = 200f,
                primaryAccountId = 1L,
            ),
            History.DebitHistory(
                id = 2L,
                date = LocalDate.of(2026, 1, 1),
                amount = 150f,
                primaryAccountId = 1L,
            ),
            History.DebitHistory(
                id = 3L,
                date = LocalDate.of(2026, 2, 2),
                amount = 120f,
                primaryAccountId = 1L,
            ),
            History.TransferHistory(
                id = 4L,
                date = LocalDate.of(2026, 3, 3),
                amount = 200f,
                primaryAccountId = 1L,
                secondaryAccountId = 2L,
            )
        )

        val repo = FakeHistoryRepository(histories)

        // AsyncPagingDataDiffer helps to get snapshot items from the PagingData
        
        val diffCallback = object : DiffUtil.ItemCallback<HistoryListItem>() {
            override fun areItemsTheSame(
                oldItem: HistoryListItem,
                newItem: HistoryListItem,
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: HistoryListItem,
                newItem: HistoryListItem,
            ): Boolean = oldItem == newItem
        }

        val updateCallback = object: ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {}
            override fun onRemoved(position: Int, count: Int) {}
            override fun onMoved(fromPosition: Int, toPosition: Int) {}
            override fun onChanged(position: Int, count: Int, payload: Any?) {}
        }

        val differ = AsyncPagingDataDiffer(
            diffCallback = diffCallback,
            updateCallback = updateCallback,
            mainDispatcher = testDispatcher,
            workerDispatcher = testDispatcher
        )

        val params = LoadHistoryParameters().apply {
            withHeaders(true)
        }

        val itemsFlow = params.getPagedHistoryListItems(repo)

        val job = launch {
            itemsFlow.collectLatest { items ->
                differ.submitData(items)
            }
        }

        // wait until page load finishes
        advanceUntilIdle()

        val actual = differ.snapshot().items

        val expected = listOf(
            HistoryListItem.Header(LocalDate.of(2026, 1, 1)),

            HistoryListItem.Item(
                data = History.CreditHistory(
                    id = 1L,
                    date = LocalDate.of(2026, 1, 1),
                    amount = 200f,
                    primaryAccountId = 1L,
                )
            ),

            HistoryListItem.Item(
                data = History.DebitHistory(
                    id = 2L,
                    date = LocalDate.of(2026, 1, 1),
                    amount = 150f,
                    primaryAccountId = 1L,
                )
            ),

            HistoryListItem.Header(LocalDate.of(2026, 2, 2)),

            HistoryListItem.Item(
                data = History.DebitHistory(
                    id = 3L,
                    date = LocalDate.of(2026, 2, 2),
                    amount = 120f,
                    primaryAccountId = 1L,
                )
            ),

            HistoryListItem.Header(LocalDate.of(2026, 3, 3)),

            HistoryListItem.Item(
                data = History.TransferHistory(
                    id = 4L,
                    date = LocalDate.of(2026, 3, 3),
                    amount = 200f,
                    primaryAccountId = 1L,
                    secondaryAccountId = 2L,
                )
            )
        )

        assertEquals(
            expected,
            actual
        )

        // flow waits for next emit, so i need to cancel the job to finish the test
        job.cancel()
    }
}

class FakeHistoryRepository(
    private val histories: List<History>
) : HistoryRepository {

    override fun getPagedHistories(params: HistoryFilterParameters)
            = flowOf(PagingData.from(histories))

    override fun getTotalCreditDebit(params: HistoryFilterParameters)
            = flowOf(HistoryTotalCreditTotalDebit())

    // ignore others
    override fun insertHistory(history: History): History = history
    override fun findHistoryById(id: Long): History? = null
    override fun getLiveHistoryById(id: Long): LiveData<History?> = MutableLiveData(null)
    override fun updateHistory(history: History): Boolean = true
    override fun deleteHistory(id: Long, reset: Boolean) = Unit
    override fun deleteMultipleHistories(ids: List<Long>, reset: Boolean) = Unit
}