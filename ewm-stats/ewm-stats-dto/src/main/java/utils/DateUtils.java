package utils;

import exception.IncorrectDateException;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@UtilityClass
public class DateUtils {

    public static LocalDateTime convertToDate(String stringDate) throws DateTimeParseException {
        try {
            return LocalDateTime.parse(stringDate, DateConstants.formatter);
        } catch (DateTimeParseException e) {
            throw new IncorrectDateException("Incorrect string format, unable to create date", stringDate);
        }
    }

    public static String convertToString(LocalDateTime date) {
        try {
            return date.format(DateConstants.formatter);
        } catch (DateTimeParseException e) {
            throw new IncorrectDateException("Incorrect date format, unable to create string.", date.toString());
        }
    }
}
