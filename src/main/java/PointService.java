import java.util.HashMap;

public class PointService {
    public static HashMap<String, Object> process(
            HashMap<String, Double> data,
            TimeMetrics workTime
    ) {
        MathCalculating calculation = new MathCalculating(data);
        boolean isHit = calculation.isInsideZone();

        HistoryManager.addRecord(
                calculation.getX(),
                calculation.getY(),
                calculation.getR(),
                isHit,
                workTime.currentTime,
                workTime.executionTimeMs
        );

        HashMap<String, Object> response = new HashMap<>();
        response.put("x", calculation.getX());
        response.put("y", calculation.getY());
        response.put("r", calculation.getR());
        response.put("isHit", isHit);
        response.put("timestamp", workTime.currentTime);
        response.put("executionTime", workTime.executionTimeMs);
        response.put("history", HistoryManager.getHistory());

        return response;
    }
}