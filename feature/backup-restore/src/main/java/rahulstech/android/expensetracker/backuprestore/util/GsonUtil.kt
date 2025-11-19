package rahulstech.android.expensetracker.backuprestore.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

//class DateType: TypeAdapter<Date>() {
//    override fun write(writer: JsonWriter?, value: Date?) {
//        writer?.let { w ->
//            if (null == value) {
//                if (w.serializeNulls) {
//                    w.nullValue()
//                }
//                return
//            }
//            w.value(value.toString())
//        }
//    }
//
//    override fun read(reader: JsonReader?): Date? {
//        return reader?.let {
//            val value = it.nextString()
//            Date.valueOf(value)
//        }
//    }
//}

fun newGson(): Gson {
    return GsonBuilder()
        .setLenient()
//        .registerTypeAdapter(Date::class.java, DateType())
        .create()
}