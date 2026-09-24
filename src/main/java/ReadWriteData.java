import com.fastcgi.FCGIInterface;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;

public class ReadWriteData {

    private final Gson gson = new Gson();


    public boolean setConnection() {
        FCGIInterface fcgi = new FCGIInterface();

        int result = fcgi.FCGIaccept();

        if (result < 0) {
            throw new IllegalStateException(
                    "FCGIaccept() вернул " + result +
                            ". Проверь FCGI_PORT и занят ли порт 24001."
            );
        }

        return true;
    }


    public String getData(Properties params) {

        String contentLengthString =
                params.getProperty("CONTENT_LENGTH");

        if (contentLengthString == null
                || contentLengthString.isBlank()) {

            throw new IllegalArgumentException(
                    "CONTENT_LENGTH отсутствует"
            );
        }

        final int contentLength;

        try {

            contentLength =
                    Integer.parseInt(
                            contentLengthString
                    );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Некорректный CONTENT_LENGTH"
            );
        }

        if (contentLength <= 0) {

            throw new IllegalArgumentException(
                    "Тело POST-запроса пустое"
            );
        }

        byte[] body =
                new byte[contentLength];

        try {

            int totalRead = 0;

            while (totalRead < contentLength) {

                int read =
                        System.in.read(
                                body,
                                totalRead,
                                contentLength - totalRead
                        );

                if (read == -1) {
                    break;
                }

                totalRead += read;
            }

            if (totalRead != contentLength) {

                throw new IllegalArgumentException(
                        "Не удалось полностью прочитать тело запроса"
                );
            }

            return new String(
                    body,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Ошибка чтения POST-запроса",
                    e
            );
        }
    }


    public HashMap<String, Double> dataFromResponse(
            String data
    ) {

        if (data == null || data.isBlank()) {

            throw new IllegalArgumentException(
                    "Пустое тело запроса"
            );
        }

        final JsonObject json;

        try {

            json =
                    gson.fromJson(
                            data,
                            JsonObject.class
                    );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Некорректный JSON"
            );
        }

        if (json == null) {

            throw new IllegalArgumentException(
                    "Некорректный JSON"
            );
        }

        if (!json.has("coord_x")
                || !json.has("coord_y")
                || !json.has("radius_r")) {

            throw new IllegalArgumentException(
                    "Необходимо передать coord_x, coord_y и radius_r"
            );
        }

        final double x;
        final double y;
        final double r;

        try {

            x = json
                    .get("coord_x")
                    .getAsDouble();

            y = json
                    .get("coord_y")
                    .getAsDouble();

            r = json
                    .get("radius_r")
                    .getAsDouble();

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "X, Y и R должны быть числами"
            );
        }

        if (!Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(r)) {

            throw new IllegalArgumentException(
                    "X, Y и R должны быть конечными числами"
            );
        }

        HashMap<String, Double> coords =
                new HashMap<>();

        coords.put("coord_x", x);
        coords.put("coord_y", y);
        coords.put("radius_r", r);

        return coords;
    }
}