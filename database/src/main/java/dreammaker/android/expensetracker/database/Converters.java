package dreammaker.android.expensetracker.database;

import android.text.TextUtils;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import dreammaker.android.expensetracker.database.model.HistoryType;

public class Converters {

    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @TypeConverter
    public static LocalDate stringToLocalDate(String date) {
        if (null == date || date.isEmpty()) {
            return null;
        }
        return LocalDate.parse(date, DATE_PATTERN);
    }

    @TypeConverter
    public static String localDateToString(LocalDate date) {
        if (null == date) {
            return null;
        }
        return date.format(DATE_PATTERN);
    }

    @TypeConverter
    public static LocalDateTime stringToLocalDateTime(String datetime) {
        if (null == datetime || datetime.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(datetime, DATETIME_PATTERN);
    }

    @TypeConverter
    public static String localDateTimeToStringTime(LocalDateTime datetime) {
        if (null == datetime) {
            return null;
        }
        return datetime.format(DATETIME_PATTERN);
    }
}
