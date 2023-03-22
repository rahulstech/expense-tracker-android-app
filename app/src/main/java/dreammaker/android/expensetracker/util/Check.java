package dreammaker.android.expensetracker.util;

import android.text.TextUtils;

public class Check {

    public static boolean isNonNull(Object o){
        return null != o;
    }

    public static void isNonNull(Object o, String message){
        isTrueOrThrow(isNonNull(o), message);
    }

    public static boolean isNull(Object o){
        return null == o;
    }

    public static void isNull(Object o, String message){
        isTrueOrThrow(isNull(o), message);
    }

    public static void isNonEmptyString(String s, String message) { isTrueOrThrow(!isEmptyString(s),message);}

    public static boolean isEmptyString(CharSequence s){
        return null == s || "".contentEquals(s);
    }

    public static boolean isEqualString(CharSequence s1, CharSequence s2){
        return s1 == s2
                || null != s1 && s1.equals(s2);
    }

    public static boolean isEquals(Object l, Object r){
        return l == r || null != l && l.equals(r);
    }

    public static <T> void isNonEmptyArray(T[] array, String message) {
        isTrueOrThrow(null != array && array.length > 0, message);
    }

    public static boolean isNumeric(String text) {
        if (null == text) return false;
        return text.matches("^(\\-){0,1}\\d{0,}(\\.){0,1}\\d+$");
    }

    private static void isTrueOrThrow(boolean check, String message) {
        if (!check) {
            throw new RuntimeException(message);
        }
    }
}
