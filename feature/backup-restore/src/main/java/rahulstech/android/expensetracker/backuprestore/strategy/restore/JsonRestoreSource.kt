package rahulstech.android.expensetracker.backuprestore.strategy.restore

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import rahulstech.android.expensetracker.backuprestore.strategy.AccountData
import rahulstech.android.expensetracker.backuprestore.strategy.GroupData
import rahulstech.android.expensetracker.backuprestore.strategy.HistoryData
import rahulstech.android.expensetracker.backuprestore.strategy.InputStreamInput
import rahulstech.android.expensetracker.backuprestore.strategy.MoneyTransferData
import rahulstech.android.expensetracker.backuprestore.strategy.Source
import rahulstech.android.expensetracker.backuprestore.strategy.TransactionData
import rahulstech.android.expensetracker.backuprestore.strategy.Constants
import java.io.InputStreamReader

class JsonRestoreSource(override val input: InputStreamInput): Source {

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private var _jsonReader: JsonReader? = null
    private val jsonReader: JsonReader get() = _jsonReader!!

    private var _cachedName: String? = null

    private fun newJsonReader(): JsonReader {
        input.destroy()
        input.create()
        val reader = gson.newJsonReader(InputStreamReader(input.get()))
        if (reader.hasNext() && reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject()
        }
        return reader
    }

    override fun setup() { _jsonReader = newJsonReader() }

    override fun cleanup() {
        _jsonReader = null
        _cachedName = null
        input.destroy()
    }

    override fun moveFirst(): Boolean = false

    override fun moveNext(): Boolean {
        _cachedName = null
        return jsonReader.hasNext()
    }

    override fun nextName(): String {
        if (jsonReader.peek() == JsonToken.NAME) {
            _cachedName = jsonReader.nextName()
            return _cachedName!!
        }
        return ""
    }

    private fun getName(): String = _cachedName ?: nextName()

    @Suppress("UNCHECKED_CAST")
    override fun <T> nextValue(): T? {
        val name = getName()
        val value = when(name) {
            Constants.JSON_FIELD_VERSION -> {
                jsonReader.nextInt()
            }
            else -> null
        }
        return value as T
    }

    override fun bufferValue(limit: Int, callback: (name: String,offset: Int, buffer: List<Any>) -> Unit) {
        val name = getName()
        val reader = jsonReader
        var offset = 0
        if (!isArrayStart(reader)) {
            return
        }
        reader.beginArray()
        while (reader.hasNext()) {
            val buffer = nextBuffer(reader,name,limit)
            callback(name, offset, buffer)
            offset += buffer.size
            if (isArrayEnd(reader)) {
                reader.endArray()
                break
            }
            if (buffer.size < limit) {
                break
            }
        }
    }

    override fun <T> readSingle(name: String): T? {
        setup()
        while (moveNext()) {
            if (nextName() == name) {
                return nextValue()
            }
        }
        cleanup()
        return null
    }

    private fun nextBuffer(reader: JsonReader, name: String, limit: Int): List<Any> {
        return when (name) {
            Constants.JSON_FIELD_ACCOUNTS -> parseArray(reader, limit) {
                parseObject(reader, AccountData::class.java)
            }

            Constants.JSON_FIELD_PEOPLE,
            Constants.JSON_FIELD_GROUPS -> parseArray(reader, limit) {
                parseObject(reader, GroupData::class.java)
            }

            Constants.JSON_FIELD_HISTORIES -> parseArray(reader, limit) {
                parseObject(reader, HistoryData::class.java)
            }

            Constants.JSON_FIELD_MONEY_TRANSFER -> parseArray(reader, limit) {
                parseObject(reader, MoneyTransferData::class.java)
            }

            Constants.JSON_FIELD_TRANSACTIONS -> parseArray(reader, limit) {
                parseObject(reader, TransactionData::class.java)
            }

            else -> emptyList()
        }
    }

    private fun <T> parseArray(reader: JsonReader, limit: Int, parser: (JsonReader)->T?): List<T> {
        val entries = mutableListOf<T>()
        while (reader.hasNext() && entries.size < limit && !isArrayEnd(reader)) {
            parser(reader)?.let { entry -> entries.add(entry) }
        }
        return entries
    }

    private fun <T> parseObject(reader: JsonReader, dataClass: Class<T>): T {
        val data = gson.fromJson<T>(reader, TypeToken.get(dataClass))
        return data
    }

    private fun isArrayStart(reader: JsonReader) = reader.peek() == JsonToken.BEGIN_ARRAY

    private fun isArrayEnd(reader: JsonReader) = reader.peek() == JsonToken.END_ARRAY
}
