
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class TimeMetrics {

    public String currentTime;
    public double executionTimeMs;


    public static long startTimer() {
        return System.nanoTime();
    }

    public static TimeMetrics finishTimer(long startTime) {
        TimeMetrics metrics = new TimeMetrics();
        long endTime = System.nanoTime();
        metrics.executionTimeMs = (endTime - startTime) / 1_000_000.0;
        OffsetDateTime now = OffsetDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        metrics.currentTime = now.format(formatter);

        return metrics;
    }
}
