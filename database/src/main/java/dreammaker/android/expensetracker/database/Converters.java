package dreammaker.android.expensetracker.database;

import android.text.TextUtils;

import androidx.room.TypeConverter;

public class Converters {

    @TypeConverter
    public static String dateToString(Date date){
        if (null != date) {
            return date.format(Date.ISO_DATE_PATTERN);
        }
        return null;
    }

    @TypeConverter
    public static Date stringToDate(String dateString){
        if (!TextUtils.isEmpty(dateString)){
            return Date.valueOf(dateString);
        }
        return null;
    }

    @TypeConverter
    public static String historyTypeToString(HistoryType type) {
        if (null == type) return null;
        return type.name();
    }

    @TypeConverter
    public static HistoryType stringToHistoryType(String type) {
        if (TextUtils.isEmpty(type)) return null;
        try {
            return HistoryType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
