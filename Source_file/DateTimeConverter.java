import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeConverter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss");

    public static LocalDateTime getLocalDateTime(String s){
        try {
            return LocalDateTime.parse(s, formatter);
        }catch(DateTimeParseException d){
            return LocalDateTime.parse(s, formatter1);
        }
    }
}
