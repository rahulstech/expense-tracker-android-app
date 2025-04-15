package dreammaker.android.expensetracker.util;

import dreammaker.android.expensetracker.database.Date;

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

    /**
     * Checks if first {@link Date Date} is a future date w.r.t the second {@link Date Date}
     *
     * @param l the expected future date
     * @param r the expected past date
     * @return {@code true} if first date comes after the second date, {@code false} if
     * at least one of the date object is null or first date does not comes after second
     */
    public static boolean isDateAfter(Date l, Date r) {
        if (l == null || r == null) return false;
        return l.getYear() > r.getYear()
                || (l.getYear() == r.getYear() && l.getMonth() > r.getMonth())
                || (l.getYear() == r.getYear() && l.getMonth() == r.getMonth()
                && l.getDayOfMonth() > r.getDayOfMonth());
    }

    public static <T> void isNonEmptyArray(T[] array, String message) {
        isTrueOrThrow(null != array && array.length > 0, message);
    }

    private static void isTrueOrThrow(boolean check, String message) {
        if (!check) {
            throw new RuntimeException(message);
        }
    }
}
