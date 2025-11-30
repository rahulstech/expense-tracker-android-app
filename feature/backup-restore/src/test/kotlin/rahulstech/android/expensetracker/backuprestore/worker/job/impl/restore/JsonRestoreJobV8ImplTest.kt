package rahulstech.android.expensetracker.backuprestore.worker.job.impl.restore

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import rahulstech.android.expensetracker.backuprestore.FakeRestoreRepositoryImpl
import rahulstech.android.expensetracker.backuprestore.asInputStream
import rahulstech.android.expensetracker.backuprestore.worker.job.JsonRestoreJob
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group
import rahulstech.android.expensetracker.domain.model.History
import java.time.LocalDate

class JsonRestoreJobV8ImplTest {

    lateinit var job: JsonRestoreJob
    lateinit var repo: FakeRestoreRepositoryImpl

    @Before
    fun setUp() {
        val json = "{" +
                "\"version\":8," +
                "\"accounts\": [" +
                "{\"id\":1,\"name\":\"Account 1\", \"balance\": 100}," +
                "{\"id\":2,\"name\":\"Account 2\",\"balance\":0,\"isDefault\":\"true\"}" +
                "]," +
                "\"groups\":[" +
                "{\"id\":1,\"name\":\"Group 1\", \"balance\": 100}," +
                "{\"id\":2,\"name\":\"Group 2\",\"balance\":0,\"isDefault\":\"true\"}" +
                "]," +
                "\"histories\":[" +
                "{\"id\":1,\"date\":\"2025-04-06\",\"type\":\"CREDIT\",\"amount\":100,\"note\":\"credit\",\"primaryAccountId\":1,\"groupId\":2}," +
                "{\"id\":2,\"date\":\"2025-04-06\",\"type\":\"DEBIT\",\"amount\":120,\"primaryAccountId\":1}," +
                "{\"id\":3,\"date\":\"2025-04-06\",\"type\":\"TRANSFER\",\"amount\":100,\"primaryAccountId\":1,\"secondaryAccountId\":2}," +
                "]" +
                "}"
        repo = FakeRestoreRepositoryImpl()
        job = JsonRestoreJobV8Impl(json.asInputStream(), repo)
    }

    @Test
    fun restore() {
        job.restore()

        val expectedAccounts = listOf(
            Account(id = 1, name = "Account 1", balance = 100f),
            Account(id = 2, name = "Account 2", balance = 0f, isDefault = true)
        )

        val expectedGroups = listOf(
            Group(id = 1, name = "Group 1", balance = 100f),
            Group(id = 2, name = "Group 2", balance = 0f, isDefault = true),
        )

        val expectedHistories = listOf(
            History.CreditHistory(id=1,date= LocalDate.of(2025,4,6), amount = 100f, note = "credit", primaryAccountId = 1, groupId = 2),
            History.DebitHistory(id=2,date= LocalDate.of(2025,4,6), amount = 120f, primaryAccountId = 1),
            History.TransferHistory(id=3,date= LocalDate.of(2025,4,6), amount = 100f, primaryAccountId = 1, secondaryAccountId = 2),
        )

        Assert.assertEquals(expectedAccounts, repo.accounts)
        Assert.assertEquals(expectedGroups, repo.groups)
        Assert.assertEquals(expectedHistories, repo.histories)
    }
}