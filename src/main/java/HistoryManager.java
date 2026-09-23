

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private static final List<PointResult> history = new ArrayList<>();

        public static void addRecord(double x, double y, double r, boolean isHit, String timestamp, double executionTime) {
        PointResult newRecord = new PointResult(x, y, r, isHit, timestamp, executionTime);
        history.add(newRecord);
    }

    public static String getHistoryAsJson() {
        Gson gson = new Gson();
        return gson.toJson(history);
    }

    public static void clearHistory() {
        history.clear();
    }
    public static List<PointResult> getHistory() {
        return history;
    }
}