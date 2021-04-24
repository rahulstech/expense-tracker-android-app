package dreammaker.android.expensetracker.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Date implements Cloneable {
    public static final String ISO_DATE_PATTERN = "yyyy-MM-dd";

    private Calendar calendar;

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
        Check.isNonEmptyString(pattern,"patter is empty");
        try{
            final java.util.Date src = new SimpleDateFormat(pattern,Locale.ENGLISH).parse(dateString);
            final Date date = new Date();
            date.calendar.setTime(src);
            return date;
        }
        catch(Exception ignore){}
        return new Date();
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

    @Override
    public Date clone()
    {
        return new Date(getYear(),getMonth(), getDayOfMonth());
    }

    public boolean isTomorrow(){
        Date today = new Date();
        return getYear() == today.getYear()
                && getMonth() == today.getMonth()
                && getDayOfMonth()-today.getDayOfMonth() == 1;
    }

    public boolean isToday(){
        Date today = new Date();
        return getYear() == today.getYear()
                && getMonth() == today.getMonth()
                && getDayOfMonth() == today.getDayOfMonth();
    }

    public boolean isYesterday(){
        Date today = new Date();
        return getYear() == today.getYear()
                && getMonth() == today.getMonth()
                && today.getDayOfMonth()-getDayOfMonth() == 1;
    }

    public Date yesterday() {
        final Calendar copy = (Calendar) this.calendar.clone();
        copy.add(Calendar.DAY_OF_YEAR,-1);
        return new Date(copy);
    }

    public Date firstDateOfThisWeek() {
        final Calendar copy = (Calendar) this.calendar.clone();
        final int firstDayOfWeek = copy.getFirstDayOfWeek();
        final int todayOfWeek = copy.get(Calendar.DAY_OF_WEEK);
        final int diff = Math.abs(firstDayOfWeek-todayOfWeek);
        copy.add(Calendar.DAY_OF_WEEK,-diff);
        return new Date(copy);
    }

    public Date lastDateOfThisWeek() {
        final Calendar copy = (Calendar) this.firstDateOfThisWeek().calendar.clone();
        copy.add(Calendar.DAY_OF_WEEK,6);
        return new Date(copy);
    }

    public Date firstDateOfLastWeek() {
        final Calendar copy = (Calendar) this.firstDateOfThisWeek().calendar.clone();
        copy.add(Calendar.DAY_OF_WEEK,-7);
        return new Date(copy);
    }

    public Date lastDateOfLastWeek() {
        final Calendar copy = (Calendar) this.lastDateOfThisWeek().calendar.clone();
        copy.add(Calendar.DAY_OF_WEEK,7);
        return new Date(copy);
    }

    public Date firstDateOfThisMonth() {
        final Calendar copy = (Calendar) this.calendar.clone();
        copy.set(Calendar.DAY_OF_MONTH,1);
        return new Date(copy);
    }

    public Date lastDateOfThisMonth() {
        final Calendar copy = (Calendar) this.calendar.clone();
        copy.set(Calendar.DAY_OF_MONTH, copy.getMaximum(Calendar.DAY_OF_MONTH));
        return new Date(copy);
    }

    public Date firstDateOfLastMonth() {
        final Calendar copy = (Calendar) this.firstDateOfThisMonth().calendar.clone();
        copy.add(Calendar.MONTH,-1);
        return new Date(copy);
    }

    public Date lastDateOfLastMonth() {
        final Calendar copy = (Calendar) this.lastDateOfThisMonth().calendar.clone();
        copy.add(Calendar.MONTH,-1);
        return new Date(copy);
    }
}
