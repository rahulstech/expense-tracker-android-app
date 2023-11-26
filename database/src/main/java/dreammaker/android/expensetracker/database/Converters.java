package dreammaker.android.expensetracker.database;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import androidx.room.TypeConverter;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.Date;

@SuppressWarnings({"unused","deprecation"})
public class Converters {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

    @TypeConverter
    @Deprecated
    public static String dateToString(Date date){
        if (null != date) {
            return date.format(Date.ISO_DATE_PATTERN);
        }
        return null;
    }

    @TypeConverter
    @Deprecated
    public static Date stringToDate(String dateString){
        if (!TextUtils.isEmpty(dateString)){
            return Date.valueOf(dateString);
        }
        return null;
    }

    @TypeConverter
    @Deprecated
    public static String bigDecimalToString(BigDecimal decimal) {
        BigDecimal scaled = decimal.setScale(2,RoundingMode.HALF_DOWN);
        return scaled.toPlainString();
    }

    @TypeConverter
    @Deprecated
    public static BigDecimal stringToBigDecimal(String text) {
        if (TextUtils.isEmpty(text)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(text).setScale(2,RoundingMode.HALF_DOWN);
    }

    @TypeConverter
    @Deprecated
    public static float bigDecimalToFloat(BigDecimal decimal) {
        BigDecimal scaled = decimal.setScale(2,RoundingMode.HALF_DOWN);
        return scaled.floatValue();
    }

    @TypeConverter
    @Deprecated
    public static BigDecimal stringToBigDecimal(float value) {
        return new BigDecimal(value).setScale(2,RoundingMode.HALF_DOWN);
    }

    @TypeConverter
    public static String currencyToString(Currency value) {
        if (null == value) return null;
        return value.toString();
    }

    @TypeConverter
    public static Currency stringToCurrency(String value) {
        if (TextUtils.isEmpty(value)) return null;
        return Currency.valueOf(value);
    }

    @TypeConverter
    public static String localDateToString(LocalDate value) {
        if (null == value) return null;
        return value.format(DATE_FORMAT);
    }

    @TypeConverter
    public static LocalDate stringToLocalDate(String value) {
        if (TextUtils.isEmpty(value)) return null;
        return LocalDate.parse(value,DATE_FORMAT);
    }
}
