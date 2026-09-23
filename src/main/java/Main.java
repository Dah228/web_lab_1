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
            if (!readData.setConnection()) {
                break;
            }

            double start = TimeMetrics.startTimer();

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

                // По условию лабораторной принимаем POST
                String method =
                        params.getProperty("REQUEST_METHOD");

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

        /*
         * У нас FastCgiExternalServer работает с -nph,
         * поэтому Java формирует ПОЛНЫЙ HTTP-ответ.
         */

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

        System.out.print(
                "Cache-Control: no-cache\r\n"
        );

        System.out.print(
                "Connection: close\r\n"
        );

        System.out.print("\r\n");

        System.out.print(json);

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