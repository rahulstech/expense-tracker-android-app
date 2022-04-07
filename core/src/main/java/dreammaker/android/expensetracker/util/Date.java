package dreammaker.android.expensetracker.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Date implements Cloneable {

    private static final String TAG = "Date";

    public static final int DAY_OF_YEAR = Calendar.DAY_OF_YEAR;
    public static final int WEEK_OF_YEAR = Calendar.WEEK_OF_YEAR;
    public static final int MONTH = Calendar.MONTH;
    public static final int YEAR = Calendar.YEAR;
    public static final int SUNDAY = Calendar.SUNDAY;
    public static final int MONDAY = Calendar.MONDAY;

    public static final String ISO_DATE_PATTERN = "yyyy-MM-dd";

    private Calendar calendar;

    /** START: Methods using {@link java.util.Calendar} API */

    /**
     * Following methods use {@link java.util.Calendar Calendar} API. In future if
     * we use some other API for date time then only the following methods to be
     * updated
     */

    Date(Calendar calendar) {
        this.calendar = calendar;
    }

    Calendar getCalendar() { return calendar; }

    private Date(){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        this.calendar = calendar;
    }

    public Date clone() {
        return new Date((Calendar) this.calendar.clone());
    }

    public Date setTimeInMillis(long millis) {
        Date copy = clone();
        copy.calendar.setTimeInMillis(millis);
        return copy;
    }

    public long getTimeInMillis() { return calendar.getTimeInMillis(); }

    public Date set(int year, int month, int dayOfMonth) {
        Date copy = clone();
        copy.calendar.set(year, month, dayOfMonth);
        return copy;
    }

    public Date setFirstDayOfWeek(int what) {
        Date copy = clone();
        copy.setFirstDayOfWeek(what);
        return copy;
    }

    public int getFirstDayOfWeek() { return calendar.getFirstDayOfWeek(); }

    public int getDayOfWeek() { return calendar.get(Calendar.DAY_OF_WEEK); }

    public int getYear(){
        return calendar.get(Calendar.YEAR);
    }

    public int getMonth(){
        return calendar.get(Calendar.MONTH);
    }

    public int getDayOfMonth(){
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Checks if the given date is a strictly past date than this
     *
     * @param date the date to test
     * @return {@code true} is test successful, {@code false} otherwise
     */
    public boolean isAfterOf(Date date) {
        int yearDiff = date.getYear()-getYear();
        int monthDiff = date.getMonth()-getMonth();
        int dayDiff = date.getDayOfMonth()-getDayOfMonth();
        return yearDiff < 0 || monthDiff < 0 || dayDiff < 0;
    }

    /**
     * Checks if the given date is a strictly future date than this
     *
     * @param date the date to test
     * @return {@code true} is test successful, {@code false} otherwise
     */
    public boolean isBeforeOf(Date date) {
        return !equals(date) && !isAfterOf(date);
    }

    /**
     * Add or deducts the weeks, months, years etc and returns a new instance after
     * change. For example:
     * this = "2022-06-07"
     * add 1 month = "2022-07-07"
     * add -2 year (i.e. deduct 2 years) = "2020-06-07"
     * add 3 weeks (assume first day of week {@link Date#SUNDAY SUNDAY}) = "2022-06-21"
     *
     * @param what any of {@link Date#YEAR} {@link Date#MONTH} {@link Date#MONTH}
     * @param quantity quantity to add or deduct
     * @return {@link Date}
     */
    public Date add(int what, int quantity) {
        Date copy = clone();
        copy.calendar.add(what,quantity);
        return copy;
    }

    /**
     * Calculates the start or end date of year, month, week etc. which this {@link Date}
     * belongs to. For example:
     * this = "2022-05-06"
     * at month start = "2022-05-01"
     * at year end = "2022-12-31"
     * etc.
     *
     * @param start {@code true} at end, {@code false} at end
     * @param of any of {@link Date#YEAR} {@link Date#MONTH} {@link Date#MONTH}
     * @return {@link Date}
     */
    public Date at(boolean start, int of) {
        int quantity = 0;
        if (start) {
            switch (of) {
                case WEEK_OF_YEAR: {
                    int firstWeekDay = getFirstDayOfWeek();
                    int currWeekDay = getDayOfWeek();
                    if (currWeekDay > firstWeekDay) quantity = firstWeekDay-currWeekDay;
                    else if (currWeekDay < firstWeekDay) quantity = firstWeekDay-currWeekDay-7;
                }
                break;
                case MONTH: quantity = 1-getDayOfMonth();
                    break;
                case YEAR: quantity = 1-calendar.get(Calendar.DAY_OF_YEAR);
                    break;
            }
        }
        else {
            switch (of) {
                case WEEK_OF_YEAR: {
                    int firstWeekDay = getFirstDayOfWeek();
                    int currWeekDay = getDayOfWeek();
                    if (currWeekDay >= firstWeekDay) quantity = 6-currWeekDay+firstWeekDay;
                    else quantity = firstWeekDay-currWeekDay-1;
                }
                break;
                case MONTH: quantity = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)-getDayOfMonth();
                    break;
                case YEAR: quantity = calendar.getActualMaximum(Calendar.DAY_OF_YEAR)-calendar.get(Calendar.DAY_OF_YEAR);
                    break;
            }
        }
        return add(DAY_OF_YEAR,quantity);
    }

    /** END: Methods using {@link java.util.Calendar} API */

    /**
     * Create new {@link Date} instance for the given date
     *
     * @param year the year
     * @param month the month. January=0 December=11
     * @param dayOfMonth the day of month, min=1 max=31
     * @return {@link Date}
     */
    public static Date of(int year, int month, int dayOfMonth) {
        return today().set(year,month,dayOfMonth);
    }

    /**
     * @return new instance of {@link Date} and date set as today
     */
    public static Date today() {
        return new Date();
    }

    /**
     * @return new instance of {@link Date} and date set as 1 day
     *          past from today
     */
    public static Date yesterday() {
        return today().add(DAY_OF_YEAR,-1);
    }

    /**
     * @return new instance of {@link Date} and date set as 1 day
     *          future from today
     */
    public static Date tomorrow() {
        return today().add(DAY_OF_YEAR,1);
    }

    /**
     * Create a new {@link Date} instance from the given date string.
     * For example:
     * dateString="2022-05-06"
     * pattern="yyyy-MM-dd"
     *
     * @param dateString non null non empty string containing date of given pattern
     *                   in app default locale
     * @param pattern the date pattern of the given date string
     * @throws {@link DateTimeException} any error occurred during parsing
     * @return {@link Date}
     */
    public static Date valueOf(String dateString, String pattern) {
        return valueOf(dateString,pattern,Locale.getDefault());
    }

    /**
     * Create a new {@link Date} instance from the given date string. This method
     * is mainly used for creating new {@link Date} instance from database date column.
     *
     * @param dateString non null non empty string containing date
     *                   of pattern {@link Date#ISO_DATE_PATTERN ISO_DATE_PATTERN}
     *                   int locale {@link Locale#ENGLISH ENGLISH}
     * @throws {@link DateTimeException} any error occurred during parsing
     * @return {@link Date}
     */
    public static Date valueOf(String dateString){
        return valueOf(dateString,ISO_DATE_PATTERN,Locale.ENGLISH);
    }

    /**
     * Create a new {@link Date} instance from the given date string.
     *
     * @param dateString non null non empty string containing date of given pattern
     * @param pattern the date pattern of the given date string
     * @param locale the locale of the string
     * @throws {@link DateTimeException} any error occurred during parsing
     * @return {@link Date}
     */
    public static Date valueOf(String dateString, String pattern, Locale locale) {
        Check.isNonEmptyString(dateString,"empty dateString");
        Check.isNonEmptyString(pattern,"empty pattern");
        Check.isNonNull(locale,"locale = null");
        try {
            final java.util.Date src = new SimpleDateFormat(pattern,locale).parse(dateString);
            return today().setTimeInMillis(src.getTime());
        }
        catch (ParseException|IllegalArgumentException ex) {
            throw new DateTimeException("unable to parse string "+dateString+" with pattern "+pattern);
        }
    }

    @Override
    public String toString() {
        return format(ISO_DATE_PATTERN,Locale.ENGLISH);
    }

    /**
     * Formats this date in given pattern in app default locale
     *
     * @param pattern output date string pattern
     * @throws {@link DateTimeException} if error occurred during formatting
     * @return formatted date string
     */
    public String format(String pattern){
        return format(pattern,Locale.getDefault());
    }
    /**
     * Formats this date in given pattern in given locale
     *
     * @param pattern output date string pattern
     * @param locale locale to use
     * @throws {@link DateTimeException} if error occurred during formatting
     * @return formatted date string
     */
    public String format(String pattern, Locale locale) {
        Check.isNonEmptyString(pattern,"empty pattern");
        Check.isNonNull(locale,"locale = null");
        try {
            return new SimpleDateFormat(pattern,locale).format(getTimeInMillis());
        }
        catch (IllegalArgumentException ex) {
            throw new DateTimeException("pattern not accepted: "+pattern);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(null != obj && obj instanceof Date){
            Date o = (Date) obj;
            return getYear() == o.getYear()
                    && getMonth() == o.getMonth()
                    && getDayOfMonth() == o.getDayOfMonth();
        }
        return false;
    }

    public boolean isTomorrow(){
        return equals(tomorrow());
    }

    public boolean isToday(){
        return equals(Date.today());
    }

    public boolean isYesterday(){
        return equals(Date.yesterday());
    }

    public Date lastWeek() {
        return add(WEEK_OF_YEAR,-1);
    }

    public Date nextWeek() {
        return add(WEEK_OF_YEAR,1);
    }

    public Date atWeekStart() {
        return at(true,WEEK_OF_YEAR);
    }

    public Date atWeekEnd() {
        return at(false,WEEK_OF_YEAR);
    }

    public Date lastMonth() {
        return add(MONTH,-1);
    }

    public Date nextMonth() {
        return add(MONTH,1);
    }

    public Date atMonthStart() {
        return at(true,MONTH);
    }

    public Date atMonthEnd() {
        return at(false,MONTH);
    }

    public Date atYearStart() {
        return at(true,YEAR);
    }

    public Date atYearEnd() {
        return at(false,YEAR);
    }
}
