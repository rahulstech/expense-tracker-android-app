package rahulstech.android.expensetracker.backuprestore.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtil {

    fun currentTimeMillis(): Long = System.currentTimeMillis()

    fun isToday(millis: Long): Boolean {
        val today = Date()
        val test = Date().apply { time = millis }
        return today.year == test.year && today.month == test.month && today.date == test.date
    }

    fun formatLastLocalBackup(millis: Long): String {
        val pattern = if (isToday(millis)) "hh:mm a" else "dd/MM/yyyy hh:mm a"
        return formatTimeMillis(millis, pattern)
    }

    fun formatLastModified(millis: Long): String {
        val pattern = if (isToday(millis)) "hh:mm a" else "dd/MM/yyyy hh:mm a"
        return formatTimeMillis(millis,pattern)
    }

    private fun formatTimeMillis(millis: Long, pattern: String): String {
        val date = Date().apply { time = millis }
        val format = SimpleDateFormat(pattern, Locale.ENGLISH)
        return format.format(date)
    }
}