import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;


public class Main {

    private final Map<String,EmpTimeData> empTimedataMap = new HashMap<>();
    public static void main(String[] args) {
        try {
            Main m = new Main();
            m.loadFileData(args[0]);
            Map<Month, Duration[]> permonthStats = m.calculateDuration();
            m.printOutput(permonthStats, args[1]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printOutput(Map<Month, Duration[]> permonthStats, String outputFilePath) throws IOException {
        File f = new File(outputFilePath);
        if(f.exists()){
            f.delete();
        }
        f.createNewFile();
        Path p = Paths.get( (new File(outputFilePath)).toURI());

        permonthStats.forEach( (mon, data)-> {
            try {
                Files.writeString(p,mon +" => "+formateDuration(data[0])+","+formateDuration(data[1])+","+formateDuration(data[2]), StandardOpenOption.APPEND);
                Files.writeString(p,System.getProperty("line.separator"), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String formateDuration(Duration duration){
        long HH = duration.toHours();
        long MM = duration.toMinutesPart();
        long SS = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", HH, MM, SS);
    }

    private   Map<Month, Duration[]>  calculateDuration() {
        final LocalTime _7_30_PM = LocalTime.of(19,30,00);
        final Map<Month, Map<String, Duration>> perMonthPerEmpDurationMap=  new HashMap<>();
        final Map<String, Map<Month, Duration>> perEmpPerMonthDurationMap=  new HashMap<>();

        empTimedataMap.forEach((emp,empMap) -> {

            perEmpPerMonthDurationMap.putIfAbsent(emp, new TreeMap<>());

                    empMap.getPerDayEvents().forEach((day, events) ->{
                        perEmpPerMonthDurationMap.get(emp).putIfAbsent(day.getMonth(),Duration.ZERO);
                        final Duration[] dayDur = { Duration.ZERO};
                        final LocalTime[] dayStartTime = {null};
                        final LocalTime[] tempStartTime = {null};
                        final LocalTime[] dayEndTime = {null};

                        events.forEach((time,entryType) ->{
                            if(entryType == EmpTimeData.EntryType.clock_in){
                                dayStartTime[0] = time;
                                tempStartTime[0] = time;
                            }
                            if(tempStartTime[0] != null && entryType == EmpTimeData.EntryType.break_start){
                                dayDur[0] = dayDur[0].plus(Duration.between(tempStartTime[0],time));
                            }
                            if(entryType == EmpTimeData.EntryType.break_stop){
                                tempStartTime[0] = time;
                            }
                            if(entryType == EmpTimeData.EntryType.clock_out){
                                dayEndTime[0] = time;
                                dayDur[0] = dayDur[0].plus(Duration.between(tempStartTime[0],time));
                            }
                        });
                        if(dayEndTime[0] == null){
                            Duration dd =  dayDur[0].plus(Duration.between(tempStartTime[0],_7_30_PM));
                            if(dd.compareTo(Duration.ofHours(6)) > 0) {
                                dd = Duration.ofHours(6);
                            }
                            dayDur[0] =dd;
                        }
                        perEmpPerMonthDurationMap.get(emp).put(day.getMonth(), perEmpPerMonthDurationMap.get(emp).get(day.getMonth()).plus(dayDur[0]));

                    });

                }
        );

        perEmpPerMonthDurationMap.forEach((emp,perMonthDurMap) -> {
            perMonthDurMap.forEach((month,dur) ->{
                perMonthPerEmpDurationMap.putIfAbsent(month,new TreeMap<>());
                perMonthPerEmpDurationMap.get(month).putIfAbsent(emp,Duration.ZERO);

                perMonthPerEmpDurationMap.get(month).put(emp,
                       perMonthPerEmpDurationMap.get(month).get(emp).plus(dur));

            });
        });

        final Map<Month, Duration[]> permonthStats = new TreeMap<>();
        perMonthPerEmpDurationMap.forEach((month, perEmpDurMap) -> {

            final Duration[] maxDur = { Duration.ZERO};
            final Duration[] minDur = { Duration.ZERO};
            final Duration[] avgDur = { Duration.ZERO};

            perEmpDurMap.forEach((emp, dur) ->{
                if(maxDur[0] == Duration.ZERO) {
                    maxDur[0] = dur;
                }else if(maxDur[0].compareTo(dur) < 1){
                    maxDur[0] = dur;
                }
                if(minDur[0] == Duration.ZERO) {
                    minDur[0] = dur;
                }else if(minDur[0].compareTo(dur) > 1){
                    minDur[0] = dur;
                }
                avgDur[0] = avgDur[0].plus(dur);
            });
            long  secondsPerEmp = avgDur[0].getSeconds() / perEmpDurMap.size();
            permonthStats.put(month, new Duration[] {maxDur[0], minDur[0], Duration.ofSeconds(secondsPerEmp)});

        });

        return permonthStats;

    }

    private  void loadFileData(String fullFilePath) throws IOException {
        Path p = Paths.get( (new File(fullFilePath)).toURI());
        List<String> lines = Files.readAllLines(p);

        lines.forEach( l -> {
            String[] word = l.split(" ");
            if (word.length == 6) {
                EmpTimeData ed =  empTimedataMap.putIfAbsent(word[0],new EmpTimeData(word[0]
                                                                    , DateTimeConverter.getLocalDateTime(word[2]+" "+word[3])
                                                                    , EmpTimeData.EntryType.getValue(word[5])));

                if(ed != null){
                    ed.addData(word[0]
                            , DateTimeConverter.getLocalDateTime(word[2]+" "+word[3])
                            , EmpTimeData.EntryType.getValue(word[5]));
                }
            }
        });

        System.out.println( "Entry count : "+empTimedataMap.size());
    }
}