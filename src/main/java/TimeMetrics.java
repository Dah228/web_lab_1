
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeMetrics {

    public String currentTime;
    public double executionTimeMs;


    public static double startTimer() {
        return System.nanoTime();
    }

    public static TimeMetrics finishTimer(double startTime) {
        TimeMetrics metrics = new TimeMetrics();
        long endTime = System.nanoTime();
        metrics.executionTimeMs = (endTime - startTime) / 1_000_000.0;
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        metrics.currentTime = now.format(formatter);

        return metrics;
    }
}