package dreammaker.android.expensetracker.text;

import android.content.Context;
import android.text.TextUtils;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;

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

    public static boolean containsIgnoreCase(CharSequence where, CharSequence what) {
        return indexOfIgnoreCase(where,what) >= 0;
    }

    public static int indexOfIgnoreCase(CharSequence where, CharSequence what) {
        if (TextUtils.isEmpty(where) || TextUtils.isEmpty(what)) {
            return -1;
        }
        return where.toString().toLowerCase().indexOf(what.toString().toLowerCase());
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
        String fn1, ln1;
        if (!TextUtils.isEmpty(firstName)) {
            fn1 = firstName.substring(0,1);
        }
        else {
            fn1 = null;
        }
        if (!TextUtils.isEmpty(lastName)) {
            ln1 = lastName.substring(0,1);
        }
        else {
            ln1 = null;
        }
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

    public static String getTransactionHistoryDescription(Context context, TransactionType type, String payer, String payee, String original) {
        switch (type) {
            case EXPENSE: {
                return context.getString(R.string.message_expense,payer);
            }
            case INCOME: {
                return context.getString(R.string.message_income,payee);
            }
            case DUE: {
                return context.getString(R.string.message_due,payee,payer);
            }
            case BORROW: {
                return context.getString(R.string.message_borrow,payer,payee);
            }
            case PAY_DUE: {
                return context.getString(R.string.message_pay_due,payer,payee);
            }
            case PAY_BORROW: {
                return context.getString(R.string.message_pay_borrow,payee,payer);
            }
            case MONEY_TRANSFER: {
                return context.getString(R.string.message_money_transfer,payer,payee);
            }
            case DUE_TRANSFER: {
                return context.getString(R.string.message_due_transfer,payer,payee);
            }
            case BORROW_TO_DUE_TRANSFER: {
                return context.getString(R.string.message_borrow_to_due_transfer,payee,payer);
            }
        }
        return original;
    }
}
