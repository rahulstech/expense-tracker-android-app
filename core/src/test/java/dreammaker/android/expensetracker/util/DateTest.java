package dreammaker.android.expensetracker.util;

import org.junit.Test;

import java.util.Calendar;
import java.util.Locale;

import static org.junit.Assert.*;

public class DateTest {

    @Test
    public void valueOf() {
        Date expected = new Date(getCalendarForDate(2022,0,6));
        Date actual = Date.valueOf("জানুয়ারী ৬, ২২","MMMM d, yy",new Locale("bn","IN"));
        assertEquals(expected,actual);
    }

    @Test
    public void format() {
        String expected = "জানুয়ারী ৬, ২২";
        String actual = Date.of(2022,0,6).format("MMMM d, yy",new Locale("bn","IN"));
        assertEquals(expected,actual);
    }

    @Test
    public void today() {
        Date expected = new Date(getCalendarForToday());
        Date actual = Date.today();
        assertEquals(expected,actual);
    }

    @Test
    public void of() {
        Date expected = new Date(getCalendarForDate(2022,3,2));
        Date actual = Date.of(2022,3, 2);
        assertEquals(expected,actual);
    }

    @Test
    public void lastWeek() {
        Date expected = new Date(getCalendarForDate(2022,2,26));
        Date actual = new Date(getCalendarForDate(2022, 3,2)).lastWeek();
        assertEquals(expected,actual);
    }

    @Test
    public void nextWeek() {
        Date expected = new Date(getCalendarForDate(2022,3,9));
        Date actual = new Date(getCalendarForDate(2022, 3,2)).nextWeek();
        assertEquals(expected,actual);
    }

    @Test
    public void atWeekStart() {
        Date expected = new Date(getCalendarForDate(2022,2,28));
        Date actual = new Date(getCalendarForDate(2022, 3,1)).atWeekStart();
        assertEquals(expected,actual);
    }

    @Test
    public void atWeekEnd() {
        Date expected = new Date(getCalendarForDate(2022,3,3));
        Date actual = new Date(getCalendarForDate(2022,3,2)).atWeekEnd();
        assertEquals(expected,actual);
    }

    @Test
    public void lastMonth() {
        Date expected = new Date(getCalendarForDate(2022,2,3));
        Date actual = new Date(getCalendarForDate(2022,3,3)).lastMonth();
        assertEquals(expected,actual);
    }

    @Test
    public void nextMonth() {
        Date expected = new Date(getCalendarForDate(2022,4,3));
        Date actual = new Date(getCalendarForDate(2022,3, 3)).nextMonth();
        assertEquals(expected,actual);
    }

    @Test
    public void atMonthStart() {
        Date expected = new Date(getCalendarForDate(2022,3,1));
        Date actual = new Date(getCalendarForDate(2022,3,2)).atMonthStart();
        assertEquals(expected,actual);
    }

    @Test
    public void atMonthEnd() {
        Date expected = new Date(getCalendarForDate(2022,3,30));
        Date actual = new Date(getCalendarForDate(2022,3,2)).atMonthEnd();
        assertEquals(expected,actual);
    }

    @Test
    public void atYearStart() {
        Date expected = new Date(getCalendarForDate(2022,0,1));
        Date actual = new Date(getCalendarForDate(2022,3,2)).atYearStart();
        assertEquals(expected,actual);
    }

    @Test
    public void atYearEnd() {
        Date expected = new Date(getCalendarForDate(2022,11,31));
        Date actual = new Date(getCalendarForDate(2022,3,2)).atYearEnd();
        assertEquals(expected,actual);
    }

    private Calendar getCalendarForToday() {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        return cal;
    }

    private Calendar getCalendarForDate(int year, int month, int dayOfMonth) {
        Calendar cal = getCalendarForToday();
        cal.set(year, month, dayOfMonth);
        return cal;
    }

    @Test
    public void isAfterOf() {
        Date pastDate = Date.of(2022,5, 6);

        Date futureDate_Day = Date.of(2022,5, 7);
        assertTrue("isAfterOf:Day", futureDate_Day.isAfterOf(pastDate));

        Date futureDate_Month = Date.of(2022,6, 6);
        assertTrue("isAfterOf:Month", futureDate_Month.isAfterOf(pastDate));

        Date futureDate_Year = Date.of(2023,5, 6);
        assertTrue("isAfterOf:Year", futureDate_Year.isAfterOf(pastDate));
    }

    @Test
    public void isBeforeOf() {
        Date futureDate = Date.of(2022,5, 6);

        Date pastDate_Day = Date.of(2022,5, 5);
        assertTrue("isBeforeOf:Day", pastDate_Day.isBeforeOf(futureDate));

        Date pastDate_Month = Date.of(2022,4, 6);
        assertTrue("isBeforeOf:Month", pastDate_Month.isBeforeOf(futureDate));

        Date pastDate_Year = Date.of(2022,5, 5);
        assertTrue("isBeforeOf:Year", pastDate_Year.isBeforeOf(futureDate));

        Date pastDate_Same = futureDate.clone();
        assertFalse("isBeforeOf:Same", pastDate_Same.isBeforeOf(futureDate));
    }
}