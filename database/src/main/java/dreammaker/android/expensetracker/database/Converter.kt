package dreammaker.android.expensetracker.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * entities are now in kotlin but dao impl codes are in java. the old Converters.java causes some null conversion issue.
 * therefore, same type converter is written in kotlin and used.
 */
object Converter {

    private val DATE_PATTERN: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val DATETIME_PATTERN: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @TypeConverter
    fun stringToLocalDate(date: String?): LocalDate? = date?.let { text ->
            LocalDate.parse(text, DATE_PATTERN)
        }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? = date?.format(DATE_PATTERN)

    @TypeConverter
    fun stringToLocalDateTime(datetime: String?): LocalDateTime? = datetime?.let { text ->
            LocalDateTime.parse(datetime, DATETIME_PATTERN)
        }

    @TypeConverter
    fun localDateTimeToStringTime(datetime: LocalDateTime?): String? = datetime?.format(DATETIME_PATTERN)
}