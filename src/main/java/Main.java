import com.fastcgi.FCGIInterface;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;

public class Main {

    private static final Gson gson = new Gson();

    public static void main(String[] args) {

        ReadWriteData readData = new ReadWriteData();

        while (true) {

            // Ждём следующий FastCGI-запрос
            readData.setConnection();

            long start = (long) TimeMetrics.startTimer();

            try {

                if (FCGIInterface.request == null) {
                    throw new IllegalStateException(
                            "FastCGI request отсутствует"
                    );
                }

                Properties params =
                        FCGIInterface.request.params;

                if (params == null) {
                    throw new IllegalStateException(
                            "FastCGI params отсутствуют"
                    );
                }

                String method =
                        params.getProperty("REQUEST_METHOD");

                // История загружается отдельно при открытии страницы.
                if ("GET".equalsIgnoreCase(method)) {
                    if (!"action=history".equals(params.getProperty("QUERY_STRING"))) {
                        sendError(400, "Bad Request", "Неизвестный запрос");
                    } else {
                        HashMap<String, Object> responseMap = new HashMap<>();
                        responseMap.put("history", HistoryManager.getHistory());
                        sendJson(200, "OK", responseMap);
                    }
                    continue;
                }

                if (!"POST".equalsIgnoreCase(method)) {
                    sendError(
                            405,
                            "Method Not Allowed",
                            "Используйте POST-запрос"
                    );
                    continue;
                }

                // Читаем тело запроса
                String requestBody =
                        readData.getData(params);

                // JSON -> координаты
                HashMap<String, Double> data =
                        readData.dataFromResponse(requestBody);

                double x = data.get("coord_x");
                double y = data.get("coord_y");
                double r = data.get("radius_r");
                if (x != Math.rint(x) || x < -5 || x > 3
                        || y < -5 || y > 3 || r < 1 || r > 4) {
                    throw new IllegalArgumentException("X: целое от -5 до 3; Y: от -5 до 3; R: от 1 до 4");
                }

                MathCalculating mathCalculating =
                        new MathCalculating(data);

                boolean isHit =
                        mathCalculating.isInsideZone();

                TimeMetrics workTime =
                        TimeMetrics.finishTimer(start);

                // Сохраняем результат между запросами
                HistoryManager.addRecord(
                        mathCalculating.getX(),
                        mathCalculating.getY(),
                        mathCalculating.getR(),
                        isHit,
                        workTime.currentTime,
                        workTime.executionTimeMs
                );

                HashMap<String, Object> responseMap =
                        new HashMap<>();

                responseMap.put(
                        "x",
                        mathCalculating.getX()
                );

                responseMap.put(
                        "y",
                        mathCalculating.getY()
                );

                responseMap.put(
                        "r",
                        mathCalculating.getR()
                );

                responseMap.put(
                        "isHit",
                        isHit
                );

                responseMap.put(
                        "timestamp",
                        workTime.currentTime
                );

                responseMap.put(
                        "executionTime",
                        workTime.executionTimeMs
                );

                responseMap.put(
                        "history",
                        HistoryManager.getHistory()
                );

                sendJson(
                        200,
                        "OK",
                        responseMap
                );

            } catch (IllegalArgumentException e) {

                sendError(
                        400,
                        "Bad Request",
                        e.getMessage()
                );

            } catch (Exception e) {

                sendError(
                        500,
                        "Internal Server Error",
                        e.getMessage() == null
                                ? "Внутренняя ошибка сервера"
                                : e.getMessage()
                );
            }
        }
    }


    private static void sendJson(
            int status,
            String statusText,
            Object response
    ) {

        String json = gson.toJson(response);

        byte[] body =
                json.getBytes(StandardCharsets.UTF_8);

        // У нас FastCgiExternalServer работает с -nph,
        //поэтому Java формирует ПОЛНЫЙ HTTP-ответ.

        System.out.print(
                "HTTP/1.1 "
                        + status
                        + " "
                        + statusText
                        + "\r\n"
        );

        System.out.print(
                "Content-Type: application/json; charset=UTF-8\r\n"
        );

        System.out.print(
                "Content-Length: "
                        + body.length
                        + "\r\n"
        );

        System.out.print("Cache-Control: no-store\r\n");

        System.out.print(
                "Connection: close\r\n"
        );

        System.out.print("\r\n");

        System.out.write(body, 0, body.length);

        System.out.flush();
    }


    private static void sendError(
            int status,
            String statusText,
            String message
    ) {

        HashMap<String, Object> error =
                new HashMap<>();

        error.put("error", message);

        sendJson(
                status,
                statusText,
                error
        );
    }
}
