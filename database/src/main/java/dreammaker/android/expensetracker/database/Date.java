package dreammaker.android.expensetracker.database;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;

public class Date implements Cloneable {

    public static final String ISO_DATE_PATTERN = "yyyy-MM-dd";

    private final Calendar calendar;

    Date(Calendar calendar) {
        this.calendar = calendar;
        this.calendar.setFirstDayOfWeek(Calendar.SUNDAY);
    }

    public Date(){
        this(Calendar.getInstance(Locale.ENGLISH));
    }

    public Date(int year, int month, int day){
        this();
        calendar.set(year, month, day);
    }

    public static Date valueOf(String dateString, String pattern) {
        if (TextUtils.isEmpty(pattern)) {
            throw new IllegalArgumentException("pattern is empty");
        }
        try{
            final java.util.Date src = new SimpleDateFormat(pattern,Locale.ENGLISH).parse(dateString);
            final Date date = new Date();
            date.calendar.setTime(src);
            return date;
        }
        catch(Exception ex){
            throw new IllegalStateException(ex);
        }
    }

    public static Date valueOf(String dateString){
        return valueOf(dateString,ISO_DATE_PATTERN);
    }

    public void set(int year, int month, int day){
        calendar.set(year, month, day);
    }

    public int getYear(){
        return calendar.get(Calendar.YEAR);
    }

    public int getMonth(){
        return calendar.get(Calendar.MONTH);
    }

    public int getDayOfMonth(){
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public long getTimeInMillis() {
        return calendar.getTimeInMillis();
    }

    @NonNull
    @Override
    public String toString() {
        return format(ISO_DATE_PATTERN);
    }

    public String format(String format){
        return new SimpleDateFormat(format,Locale.ENGLISH).format(calendar.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Date){
            return equals((Date) o);
        }
        return false;
    }

    public boolean equals(Date o){
        if(this == o) return true;
        if(null != o){
            return getYear() == o.getYear()
                    && getMonth() == o.getMonth()
                    && getDayOfMonth() == o.getDayOfMonth();
        }
        return false;
    }

    @NonNull
    @Override
    public Date clone()
    {
        return new Date(getYear(),getMonth(), getDayOfMonth());
    }

    public Date lastDateOfThisMonth() {
        final Calendar copy = (Calendar) this.calendar.clone();
        copy.set(Calendar.DAY_OF_MONTH, copy.getActualMaximum(Calendar.DAY_OF_MONTH));
        return new Date(copy);
    }

    public Date plusDays(int n) {
        LocalDate date = LocalDate.of(getYear(), getMonth()+1, getDayOfMonth());
        LocalDate newDate = date.plusDays(n);
        return new Date(newDate.getYear(), newDate.getMonthValue()-1, newDate.getDayOfMonth());
    }

    public Date plusMonths(int n) {
        LocalDate date = LocalDate.of(getYear(), getMonth()+1, getDayOfMonth());
        LocalDate newDate = date.plusMonths(n);
        return new Date(newDate.getYear(), newDate.getMonthValue()-1, newDate.getDayOfMonth());
    }

    public Date plusYears(int n) {
        LocalDate date = LocalDate.of(getYear(), getMonth()+1, getDayOfMonth());
        LocalDate newDate = date.plusYears(n).minusMonths(1);
        return new Date(newDate.getYear(), newDate.getMonthValue()-1, newDate.getDayOfMonth());
    }

    public static int durationDays(Date from, Date to) {
        LocalDate start = LocalDate.of(from.getYear(), from.getMonth()+1, from.getDayOfMonth());
        LocalDate end = LocalDate.of(to.getYear(), to.getMonth()+1, to.getDayOfMonth());
        return (int) ChronoUnit.DAYS.between(start, end);
    }

    public static int durationMonths(Date from, Date to) {
        LocalDate start = LocalDate.of(from.getYear(), from.getMonth()+1, from.getDayOfMonth());
        LocalDate end = LocalDate.of(to.getYear(), to.getMonth()+1, to.getDayOfMonth());
        return (int) ChronoUnit.MONTHS.between(start, end);
    }
}
