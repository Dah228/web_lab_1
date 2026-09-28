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

            long start = TimeMetrics.startTimer();

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


                HashMap<String, Double> data =
                        readData.dataFromResponse(requestBody);

                validateCoordinates(data);

                MathCalculating mathCalculating =
                        new MathCalculating(data);

                boolean isHit =
                        mathCalculating.isInsideZone();

                TimeMetrics workTime =
                        TimeMetrics.finishTimer(start);

                sendJson(200, "OK", PointService.process(data, workTime));

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

    private static void validateCoordinates(HashMap<String, Double> data) {
        Double x = data.get("coord_x");
        Double y = data.get("coord_y");
        Double r = data.get("radius_r");

        if (x == null || y == null || r == null
                || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(r)
                || x != Math.rint(x) || x < -5 || x > 3
                || y < -3 || y > 5
                || r < 1 || r > 5) {
            throw new IllegalArgumentException(
                    "X: целое от -5 до 3; Y: от -3 до 5; R: от 1 до 5"
            );
        }
    }
}
