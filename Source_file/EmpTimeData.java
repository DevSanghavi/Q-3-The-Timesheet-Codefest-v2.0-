import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class EmpTimeData {
    private String empId;

    public Map<LocalDate, Map<LocalTime, EntryType>> getPerDayEvents() {
        return perDayEvents;
    }

    final Map<LocalDate,Map<LocalTime,EntryType>> perDayEvents = new TreeMap<>();
    public EmpTimeData(String empId,LocalDateTime dateTime, EntryType entryType) {
        this.empId = empId;
        perDayEvents.putIfAbsent(dateTime.toLocalDate(),new TreeMap<>());
        perDayEvents.get(dateTime.toLocalDate()).putIfAbsent(dateTime.toLocalTime(),entryType);
    }

    public void addData(String s, LocalDateTime dateTime, EntryType entryType) {
        perDayEvents.putIfAbsent(dateTime.toLocalDate(),new TreeMap<>());
        perDayEvents.get(dateTime.toLocalDate()).putIfAbsent(dateTime.toLocalTime(),entryType);
    }

    public enum EntryType{
        clock_in("clock-in"),
        break_start("break-start"),
        break_stop("break-stop"),
        clock_out("clock-out");

        public String getEntryName() {
            return entryName;
        }

        private String entryName;
        static final Map<String,EntryType> m = new HashMap<>();

        static{
            Arrays.stream(EntryType.values()).forEach(v -> m.put(v.getEntryName(),v));
        }
        EntryType(String s){
            this.entryName = s;
        }

        static EntryType  getValue(String s){
            return m.get(s);
        }

    }
}
