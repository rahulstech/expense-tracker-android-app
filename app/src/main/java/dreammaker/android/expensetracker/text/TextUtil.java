package dreammaker.android.expensetracker.text;

import android.content.Context;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.database.type.Currency;

@SuppressWarnings("unused")
public class TextUtil {

    public static final int FIRST_NAME_FIRST = 1;

    public static final int LAST_NAME_FIRST = 2;

    public static boolean isNonLetter(String s) {
        return null != s && s.matches("[\\s\\d!-/:-@\\[-`{-~\\]]");
    }

    public static boolean isNumber(String text) {
        if (null == text) return false;
        return text.matches("^(\\-){0,1}\\d{0,}(\\.){0,1}\\d+$");
    }

    public static boolean containsIgnoreCase(String where, String what) {
        if (TextUtils.isEmpty(where) || TextUtils.isEmpty(what)) {
            return false;
        }
        return where.toLowerCase().contains(what.toLowerCase());
    }

    @NonNull
    public static String getDisplayLabel(@NonNull String text) {
        return getDisplayLabel(text,1);
    }

    @NonNull
    public static String getDisplayLabel(@NonNull String text, int len) {
        String first = text.substring(0,len);
        if (first.matches("[A-Za-z]")) {
            return first.toUpperCase(Locale.ENGLISH);
        }
        else if (isNonLetter(first)) {
            return "#";
        }
        else {
            return first;
        }
    }

    @NonNull
    public static String getDisplayNameForPerson(String firstName, String lastName, int orientation) {
        if (orientation == LAST_NAME_FIRST) {
            return getDisplayNameForPerson(lastName,firstName,FIRST_NAME_FIRST);
        }
        final boolean hasFN = !TextUtils.isEmpty(firstName);
        final boolean hasLN = !TextUtils.isEmpty(lastName);
        if (!hasFN && !hasLN) {
            return "";
        }
        if (!hasLN) {
            return firstName;
        }
        return lastName;
    }

    public static String getDisplayNameForPerson(String firstName, String lastName, boolean firstNameFirst, String ifEmpty) {
        if (!firstNameFirst) {
            return getDisplayNameForPerson(lastName,firstName,true, ifEmpty);
        }
        final boolean hasFN = !TextUtils.isEmpty(firstName);
        final boolean hasLN = !TextUtils.isEmpty(lastName);
        if (hasFN &&  hasLN) {
            return firstName+" "+lastName;
        }
        else if (hasFN) {
            return firstName;
        }
        else if (hasLN) {
            return lastName;
        }
        return ifEmpty;
    }

    public static String getDisplayLabelForPerson(String firstName, String lastName, boolean firstNameFirst, String ifEmpty) {
        if (!firstNameFirst) {
            return getDisplayLabelForPerson(lastName,firstName,true,ifEmpty);
        }
        String fn1 = substring(firstName.trim(),0,1,null);
        String ln1 = substring(lastName.trim(),0,1,null);
        String label;
        if (null != fn1 && null != ln1) {
            label = fn1+ln1;
        }
        else if (null != fn1) {
            label = fn1;
        }
        else if (null != ln1){
            label = ln1;
        }
        else {
            return ifEmpty;
        }
        return label.toUpperCase();
    }

    private static String substring(String source, int start, int length, String otherwise) {
        if (TextUtils.isEmpty(source)) {
            return otherwise;
        }
        return source.substring(start,start+length-1);
    }

    @NonNull
    public static CharSequence currencyToText(@NonNull Context context, @NonNull Currency currency) {
        return "";
    }

    @Nullable
    public static Currency tryConvertToCurrencyOrNull(CharSequence text) {
        if (null == text) return null;
        try {
            return Currency.valueOf(text.toString());
        }
        catch (Throwable ignore) {}
        return null;
    }
}
