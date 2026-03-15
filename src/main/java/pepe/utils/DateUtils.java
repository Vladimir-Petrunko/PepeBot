package pepe.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateUtils {
    public static String getCurrentDate() {
        return LocalDate.now(ZoneId.of("Europe/Moscow")).toString();
    }

    public static String toCanonical(String date) {
        String[] parts = date.split("-");
        return parts[2] + "." + parts[1] + "." + parts[0];
    }

    public static LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now(ZoneId.of("Europe/Moscow"));
    }
}
