package rahulstech.android.expensetracker.backuprestore.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDate
import java.time.LocalDateTime

// NOTE: in TypeAdapter if value null then write null
// no need to check writer.serializeNulls and write null deterministic,
// it will cause a runtime error and stack track is not very clear to understand this cause

class LocalDateType: TypeAdapter<LocalDate>() {
    override fun write(writer: JsonWriter, value: LocalDate?) {
        writer.serializeNulls
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(value.toString())
        }
    }

    override fun read(reader: JsonReader): LocalDate? {
        return if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            LocalDate.parse(reader.nextString())
        }
    }
}

class LocalDateTimeType : TypeAdapter<LocalDateTime>() {

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toString())
        }
    }

    override fun read(reader: JsonReader): LocalDateTime? {
        return if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            LocalDateTime.parse(reader.nextString())
        }
    }
}


fun newGson(): Gson {
    return GsonBuilder()
        .setStrictness(Strictness.LENIENT)
        .registerTypeAdapter(LocalDate::class.java, LocalDateType())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeType())
        .create()
}