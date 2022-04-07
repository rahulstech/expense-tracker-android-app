package dreammaker.android.expensetracker.util;

import android.icu.text.DateTimePatternGenerator;

public class DateTimeException extends RuntimeException {

    public DateTimeException(String message) {
        super(message);
    }

    public DateTimeException(String message, Throwable parent) {
        super(message,parent);
    }
}
