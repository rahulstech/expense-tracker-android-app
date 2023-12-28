package dreammaker.android.expensetracker.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DateTimeUtil {

    private DateTimeUtil() {}

    public static long toUTCMillis(LocalDate date) {
        return ZonedDateTime.of(date, LocalTime.MIDNIGHT, ZoneId.ofOffset("UTC", ZoneOffset.UTC))
                .toInstant().toEpochMilli();
    }

    public static LocalDateTime utcMillisToLocalDateTime(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
