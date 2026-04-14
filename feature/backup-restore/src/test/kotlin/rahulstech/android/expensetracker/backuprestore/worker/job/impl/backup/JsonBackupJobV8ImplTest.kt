package rahulstech.android.expensetracker.backuprestore.worker.job.impl.backup

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Test
import rahulstech.android.expensetracker.backuprestore.FakeBackupRepositoryImpl
import java.io.ByteArrayOutputStream

class JsonBackupJobV8ImplTest {

    @Test
    fun backup() {

        val repo = FakeBackupRepositoryImpl()
        val dest = ByteArrayOutputStream()
        val job = JsonBackupJobV8Impl(dest, repo)

        job.backup()

        val expected = """
            {
                "version":8,
                "accounts":[
                    {
                        "name":"Account 1",
                        "balance":100.0,
                        "id":1,
                        "totalUsed":0,
                        "isDefault": false
                    },
                    {
                        "name":"Account 2",
                        "balance":0.0,
                        "id":2,
                        "totalUsed":0,
                        "isDefault": true
                    }
                ],
                "groups":[
                    {
                        "name":"Group 1",
                        "balance":100.0,
                        "id":1,
                        "totalUsed":0
                    },
                    {
                        "name":"Group 2",
                        "balance":0.0,
                        "id":2,
                        "totalUsed":0
                    }
                ],
                "histories":[
                    {
                        "id":1,
                        "type":"CREDIT",
                        "amount":100.0,
                        "date":"2025-04-06",
                        "primaryAccountId":1,
                        "groupId":2,
                        "note":"credit"
                    },
                    {
                        "id":2,
                        "type":"DEBIT",
                        "amount":120.0,
                        "date":"2025-04-06",
                        "primaryAccountId":1
                    },
                    {
                        "id":3,
                        "type":"TRANSFER",
                        "amount":100.0,
                        "date":"2025-04-06","primaryAccountId":1,
                        "secondaryAccountId":2
                    }
                ]
            }
        """.trimIndent()

        val actual = dest.toString(Charsets.UTF_8)

        val expectedTree = JsonParser.parseString(expected)
        val actualTree = JsonParser.parseString(actual)

        assertEquals(expectedTree, actualTree)
    }
}
