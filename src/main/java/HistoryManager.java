import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private static final Gson gson =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();


     // Файл будет лежать рядом с запущенным JAR,

    private static final Path HISTORY_FILE =
            Path.of("history.json");

    private static final List<PointResult> history =
            new ArrayList<>();


    static {
        loadHistoryFromFile();
    }


    public static synchronized void addRecord(
            double x,
            double y,
            double r,
            boolean isHit,
            String timestamp,
            double executionTime
    ) {

        PointResult newRecord =
                new PointResult(
                        x,
                        y,
                        r,
                        isHit,
                        timestamp,
                        executionTime
                );

        history.add(newRecord);

        saveHistoryToFile();
    }


    public static synchronized List<PointResult> getHistory() {
        return new ArrayList<>(history);
    }


    public static synchronized void clearHistory() {

        history.clear();

        saveHistoryToFile();
    }


    private static void saveHistoryToFile() {

        try {

            String json =
                    gson.toJson(history);

            Files.writeString(
                    HISTORY_FILE,
                    json,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

        } catch (IOException e) {

            System.err.println(
                    "Не удалось сохранить историю в файл: "
                            + e.getMessage()
            );
        }
    }


    private static void loadHistoryFromFile() {

        if (!Files.exists(HISTORY_FILE)) {
            return;
        }

        try {

            String json =
                    Files.readString(
                            HISTORY_FILE,
                            StandardCharsets.UTF_8
                    );

            if (json.isBlank()) {
                return;
            }

            List<PointResult> loadedHistory =
                    gson.fromJson(
                            json,
                            new TypeToken<List<PointResult>>() {
                            }.getType()
                    );

            if (loadedHistory != null) {
                history.addAll(loadedHistory);
            }

        } catch (Exception e) {

            System.err.println(
                    "Не удалось загрузить историю из файла: "
                            + e.getMessage()
            );
        }
    }
}