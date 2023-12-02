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
