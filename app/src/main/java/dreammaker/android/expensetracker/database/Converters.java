package dreammaker.android.expensetracker.database;

import androidx.room.TypeConverter;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;

public class Converters {

    @TypeConverter
    public static String dateToString(Date date){
        if (Check.isNonNull(date)) {
            return date.format(Date.ISO_DATE_PATTERN);
        }
        return null;
    }

    @TypeConverter
    public static Date stringToDate(String dateString){
        if (!Check.isEmptyString(dateString)){
            return Date.valueOf(dateString);
        }
        return null;
    }
}
