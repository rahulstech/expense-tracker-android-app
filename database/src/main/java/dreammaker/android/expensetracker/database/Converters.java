package dreammaker.android.expensetracker.database;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import androidx.room.TypeConverter;
import dreammaker.android.expensetracker.database.type.Date;
import dreammaker.android.expensetracker.database.type.TransactionType;

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
    public static String bigDecimalToString(BigDecimal decimal) {
        BigDecimal scaled = decimal.setScale(2,RoundingMode.HALF_DOWN);
        return scaled.toPlainString();
    }

    @TypeConverter
    public static BigDecimal stringToBigDecimal(String text) {
        if (TextUtils.isEmpty(text)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(text).setScale(2,RoundingMode.HALF_DOWN);
    }

    @TypeConverter
    public static float bigDecimalToFloat(BigDecimal decimal) {
        BigDecimal scaled = decimal.setScale(2,RoundingMode.HALF_DOWN);
        return scaled.floatValue();
    }

    @TypeConverter
    public static BigDecimal stringToBigDecimal(float value) {
        return new BigDecimal(value).setScale(2,RoundingMode.HALF_DOWN);
    }

    @TypeConverter
    public static String transactionTypeToString(TransactionType type) {
        return type.name();
    }

    @TypeConverter
    public static TransactionType stringToTransactionType(String name) {
        return TransactionType.valueOf(name);
    }
}
