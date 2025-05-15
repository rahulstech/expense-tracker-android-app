package rahulstech.android.expensetracker.backuprestore.strategy.json.restore

import junit.framework.TestCase.assertEquals
import org.junit.Test
import rahulstech.android.expensetracker.backuprestore.strategy.AccountData
import rahulstech.android.expensetracker.backuprestore.strategy.GroupData
import rahulstech.android.expensetracker.backuprestore.strategy.HistoryData
import rahulstech.android.expensetracker.backuprestore.strategy.InputStreamInput
import rahulstech.android.expensetracker.backuprestore.strategy.MoneyTransferData
import rahulstech.android.expensetracker.backuprestore.strategy.TransactionData
import rahulstech.android.expensetracker.backuprestore.strategy.restore.JsonRestoreSource
import java.io.ByteArrayInputStream

class JsonRestoreSourceTest {

    private fun getInputStreamInput(json: String): InputStreamInput {
        return InputStreamInput { ByteArrayInputStream(json.toByteArray(Charsets.UTF_8)) }
    }

    @Test
    fun testReadAccounts() {
        val json = "{\"accounts\": [" +
                "{\"_id\": 1, \"account_name\": \"Account 1\", \"balance\": 150.00}," +
                "{\"id\": 2, \"name\": \"Account 2\", \"balance\": -140.33}" +
                "]}"
        val input = getInputStreamInput(json)
        val source = JsonRestoreSource(input)
        val actual = mutableListOf<Any>()
        source.setup()
        source.moveNext()
        source.bufferValue(2) { _,_,buffer -> actual.addAll(buffer) }
        source.cleanup()
        val expected = listOf(
            AccountData(1,"Account 1", 150.00f),
            AccountData(2,"Account 2", -140.33f)
        )
        assertEquals(expected,actual)
    }

    @Test
    fun testReadPeople() {
        val json = "{\"people\": [" +
                "{\"_id\": 1, \"person_name\": \"Person 1\", \"due\": 150.00}," +
                "]}"
        val input = getInputStreamInput(json)
        val source = JsonRestoreSource(input)
        val actual = mutableListOf<Any>()
        source.setup()
        source.moveNext()
        source.bufferValue(1) { _,_,buffer -> actual.addAll(buffer) }
        source.cleanup()
        val expected = listOf(
            GroupData(1,"Person 1", 150.00f)
        )
        assertEquals(expected,actual)
    }

    @Test
    fun testReadGroups() {
        val json = "{\"groups\": [" +
                "{\"id\": 1, \"name\": \"Group 1\", \"balance\": -140.33}" +
                "]}"
        val input = getInputStreamInput(json)
        val source = JsonRestoreSource(input)
        val actual = mutableListOf<Any>()
        source.setup()
        source.moveNext()
        source.bufferValue(1) { _,_,buffer -> actual.addAll(buffer) }
        source.cleanup()
        val expected = listOf(
            GroupData(1,"Group 1", -140.33f)
        )
        assertEquals(expected,actual)
    }

    @Test
    fun testReadTransactions() {
        val json = "{\"transactions\":[" +
                "{\"_id\": 1, \"amount\": 150.00, \"date\": \"2023-05-16\", \"type\": 0, \"deleted\": false, \"account_id\": 1, \"person_id\": 1, \"description\": null}," +
                "{\"_id\": 2, \"amount\": 150.00, \"date\": \"2023-05-16\", \"type\": 0, \"deleted\": true, \"account_id\": 1, \"person_id\": 1, \"description\": null}," +
                "{\"_id\": 3, \"amount\": 150.00, \"date\": \"2023-05-16\", \"type\": 1, \"deleted\": false, \"account_id\": 1, \"person_id\": null, \"description\": \"expense\"}," +
                "]}"
        val input = getInputStreamInput(json)
        val source = JsonRestoreSource(input)
        val actual = mutableListOf<Any>()
        source.setup()
        source.moveNext()
        source.bufferValue(3) { _,_,buffer -> actual.addAll(buffer) }
        source.cleanup()
        val expected = listOf(
            TransactionData(1,150.00f,"2023-05-16",1,1, null, 0, false),
            TransactionData(2,150.00f,"2023-05-16",1,1, null, 0, true),
            TransactionData(3,150.00f,"2023-05-16",1,null, "expense", 1, false)
        )
        assertEquals(expected,actual)
    }

    @Test
    fun testReadMoneyTransfers() {
        val json = "{\"money_transfers\": [" +
                "{\"id\": 1, \"amount\": 150.00, \"when\": \"2023-05-16\", \"payee_account_id\": 2, \"payer_account_id\": 1, \"description\": null}, " +
                "{\"id\": 2, \"amount\": 140.33, \"when\": \"2023-05-16\", \"payee_account_id\": 2, \"payer_account_id\": 1, \"description\": \"transfer\"}" +
                "]}"
        val input = getInputStreamInput(json)
        val source = JsonRestoreSource(input)
        val actual = mutableListOf<Any>()
        source.setup()
        source.moveNext()
        source.bufferValue(2) { _,_,buffer -> actual.addAll(buffer) }
        source.cleanup()
        val expected = listOf(
            MoneyTransferData(1,150.00f,"2023-05-16",1,2,null),
            MoneyTransferData(2,140.33f,"2023-05-16",1,2,"transfer")
        )
        assertEquals(expected,actual)
    }

    @Test
    fun testReadHistories() {
        val json = "{\"histories\": [" +
                "{\"id\": 1, \"type\": \"CREDIT\", \"primaryAccountId\": 1, \"secondaryAccountId\": null, \"groupId\": 2, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": null}," +
                "{\"id\": 2, \"type\": \"DEBIT\", \"primaryAccountId\": 1, \"secondaryAccountId\": null, \"groupId\": 2, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": null}," +
                "{\"id\": 3, \"type\": \"TRANSFER\", \"primaryAccountId\": 1, \"secondaryAccountId\": 2, \"groupId\": null, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": \"transfer\"}," +
                "{\"id\": 4, \"type\": \"CREDIT\", \"primaryAccountId\": 1, \"secondaryAccountId\": null, \"groupId\": null, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": \"income\"}" +
                "]}"
        val input = getInputStreamInput(json)
        val source = JsonRestoreSource(input)
        val actual = mutableListOf<Any>()
        source.setup()
        source.moveNext()
        source.bufferValue(4) { _,_, buffer -> actual.addAll(buffer) }
        source.cleanup()
        val expected = listOf(
            HistoryData(1,"CREDIT", 1,null,2,150.00f,"2023-05-16",null) ,
            HistoryData(2,"DEBIT", 1,null,2,150.00f,"2023-05-16",null),
            HistoryData(3,"TRANSFER", 1,2,null,150.00f,"2023-05-16","transfer"),
            HistoryData(4,"CREDIT", 1,null,null,150.00f,"2023-05-16","income")
        )
        assertEquals(expected,actual)
    }

    @Test
    fun testReadSingle() {
        val json = "{\"version\": 1}"
        val input = getInputStreamInput(json)
        val source = JsonRestoreSource(input)
        source.setup()
        source.moveNext()
        val actual = source.readSingle<Int>("version")
        source.cleanup()
        val expected = 1
        assertEquals(expected,actual)
    }

    @Test
    fun testReadBuffer() {
        val json = "{\"histories\": [" +
                "{\"id\": 1, \"type\": \"CREDIT\", \"primaryAccountId\": 1, \"secondaryAccountId\": null, \"groupId\": 2, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": null}," +
                "{\"id\": 2, \"type\": \"DEBIT\", \"primaryAccountId\": 1, \"secondaryAccountId\": null, \"groupId\": 2, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": null}," +
                "{\"id\": 3, \"type\": \"TRANSFER\", \"primaryAccountId\": 1, \"secondaryAccountId\": 2, \"groupId\": null, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": \"transfer\"}," +
                "{\"id\": 4, \"type\": \"CREDIT\", \"primaryAccountId\": 1, \"secondaryAccountId\": null, \"groupId\": null, \"amount\": 150.00, \"date\": \"2023-05-16\", \"note\": \"income\"}" +
                "]}"
        val input = getInputStreamInput(json)
        val source = JsonRestoreSource(input)
        val actual = mutableListOf<List<Any>>()
        source.setup()
        source.moveNext()
        source.bufferValue(3) { _,_,buffer -> actual.add(buffer) }
        source.cleanup()
        val expected = listOf(
            listOf(
                HistoryData(1,"CREDIT", 1,null,2,150.00f,"2023-05-16",null) ,
                HistoryData(2,"DEBIT", 1,null,2,150.00f,"2023-05-16",null),
                HistoryData(3,"TRANSFER", 1,2,null,150.00f,"2023-05-16","transfer")
            ),
            listOf(
                HistoryData(4,"CREDIT", 1,null,null,150.00f,"2023-05-16","income")
            )
        )
        assertEquals(expected,actual)
    }

    @Test
    fun testReadDocument() {
        val json = "{\"version\": 1," +
                "\"accounts\": [{\"id\": 1, \"name\": \"Account 1\", \"balance\": 150.00}], " +
                "\"groups\": [{\"id\": 2, \"name\": \"Group 2\", \"balance\": -140.33}]" +
                "}"
        val input = getInputStreamInput(json)
        val source = JsonRestoreSource(input)
        var version = -1
        val accounts = mutableListOf<Any>()
        val groups = mutableListOf<Any>()
        source.setup()
        while (source.moveNext()) {
            when(source.nextName()) {
                "version" -> version = source.nextValue<Int>()!!
                "accounts" -> source.bufferValue(1) { name,offset,buffer ->
                    println("$name=$buffer offset=$offset")
                    accounts.addAll(buffer)
                }
                "groups" -> source.bufferValue(1) { _,offset,buffer ->
                    println("group=$buffer offset=$offset")
                    groups.addAll(buffer)
                }
            }
        }
        source.cleanup()

        assertEquals("version",1, version)
        assertEquals("accounts",listOf(AccountData(1,"Account 1", 150.00f)), accounts)
        assertEquals("groups",listOf(GroupData(2,"Group 2",-140.33f)), groups)
    }
}