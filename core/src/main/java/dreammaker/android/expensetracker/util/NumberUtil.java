package dreammaker.android.expensetracker.util;

import android.content.Context;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import androidx.annotation.NonNull;

public class NumberUtil {

    private static final DecimalFormat decimalFormat = new DecimalFormat("##,##,##,##,###.##");

    /**
     *
     * @param n
     * @return
     */
    public static String format(double n) {
        return format(n, Locale.ENGLISH);
    }

    public static String format(double n, @NonNull Locale target) {
        long i = (long) n;
        String o = "";
        int d = 1000;
        int r;
        boolean firstGroup = true;
        do {
            r = (int) (i%d);
            o = firstGroup ? String.valueOf(r) : r+","+o;
            firstGroup = false;
            i = i/d;
            d = 100;
        }
        while (i > 0);
        int decimal = (int) (Math.round(n*100))%100;
        if (decimal > 9) {
            decimal = decimal%10 == 0 ? decimal/10 : decimal;
            o = o+String.format(".%d",decimal);
        }
        else if (decimal > 0) o = o+".0"+decimal;

        return o;
    }

    public static double parse(String s) {
        try {
            return decimalFormat.parse(s).doubleValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String currencyToWord(@NonNull Context context, float amount) {
        String[] words = numberToWords(context,amount);
        // TODO: return currency string
        return null;
    }

    public static String currencyToWord(@NonNull Context context, @NonNull Locale target, float amount) {
        String[] words = numberToWords(context,target,amount);
        // TODO: return currency string
        return null;
    }

    public static String[] numberToWords(@NonNull Context context, double n) {
        return NumberToWord.getCachedInstance(context).numberToWords(n);
    }

    public static String[] numberToWords(@NonNull Context context, @NonNull Locale target, double n) {
        return NumberToWord.newInstance(context,target).numberToWords(n);
    }
}
