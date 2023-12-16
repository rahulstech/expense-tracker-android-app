package dreammaker.android.expensetracker.text;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;

@SuppressWarnings("unused")
public class TextUtil {

    public static final String DEFAULT_DISPLAY_LABEL_NON_LETTER = "#";

    @Deprecated
    public static boolean isNonLetter(String s) {
        return null == s || s.matches("[\\s\\d!-/:-@\\[-`{-~\\]]");
    }

    public static boolean isLetter(int codePoint) {
        return Character.isLetter(codePoint);
    }

    public static boolean isNumber(String text) {
        return null != text && text.matches("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
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

    public static String getDisplayLabelLetterOnly(String text, String ifEmpty) {
        if (TextUtils.isEmpty(text)) {
            return ifEmpty;
        }
        String first = text.substring(0,1);
        int code = text.codePointAt(0);
        if (isLetter(code)) {
            return first.toUpperCase();
        }
        return ifEmpty;
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

    public static String getDisplayLabelForAccount(String name) {
        return getDisplayLabelForAccount(name,null);
    }

    public static String getDisplayLabelForAccount(String name, String ifEmpty) {
        if (TextUtils.isEmpty(name)) {
            return ifEmpty;
        }
        String first = name.substring(0,1);
        return first.toUpperCase();
    }

    public static String getDisplayLabelForPerson(String firstName, String lastName, boolean firstNameFirst) {
        return getDisplayLabelForPerson(firstName,lastName,firstNameFirst,null);
    }

    public static String getDisplayLabelForPerson(String firstName, String lastName, boolean firstNameFirst, String isEmpty) {
        if (!firstNameFirst) {
            return getDisplayLabelForPerson(lastName,firstName,true, isEmpty);
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
            return isEmpty;
        }
        return label.toUpperCase();
    }

    public static String prettyFormatCurrency(Currency currency) {
        return currency.toString();
    }

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

    public static String getTransactionHistoryDescription(Resources res, TransactionType type,
                                                          @Nullable CharSequence payer, @Nullable String payee, String original) {
        if (!TextUtils.isEmpty(original)) {
            return original;
        }
        if (null == payee) {
            payee = res.getString(R.string.label_unknown);
        }
        if (null == payer) {
            payer = res.getString(R.string.label_unknown);
        }
        switch (type) {
            case EXPENSE: {
                return res.getString(R.string.message_expense,payer);
            }
            case INCOME: {
                return res.getString(R.string.message_income,payee);
            }
            case DUE: {
                return res.getString(R.string.message_due,payee,payer);
            }
            case BORROW: {
                return res.getString(R.string.message_borrow,payer,payee);
            }
            case PAY_DUE: {
                return res.getString(R.string.message_pay_due,payer,payee);
            }
            case PAY_BORROW: {
                return res.getString(R.string.message_pay_borrow,payee,payer);
            }
            case MONEY_TRANSFER: {
                return res.getString(R.string.message_money_transfer,payer,payee);
            }
            case DUE_TRANSFER: {
                return res.getString(R.string.message_due_transfer,payer,payee);
            }
            case BORROW_TO_DUE_TRANSFER: {
                return res.getString(R.string.message_borrow_to_due_transfer,payer,payee);
            }
            case BORROW_TRANSFER: {
                return res.getString(R.string.message_borrow_transfer,payer,payee);
            }
        }
        return null;
    }
}
