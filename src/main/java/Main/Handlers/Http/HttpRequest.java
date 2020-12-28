package Main.Handlers.Http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String httpVersion;
    private String method;
    private String path;
    private Map<String, String> params;
    private Map<String, String> headers;

    private HttpRequest()  {
        httpVersion = "";
        method = "";
        params = new HashMap<>();
        headers = new HashMap<>();
    }

    public static HttpRequest wait(BufferedReader reader) throws IOException {
        HttpRequest httpRequest = null;
        boolean requestReceived = false;
        while (!requestReceived) {
            String startLine = reader.readLine();
            if (startLine != null && !startLine.trim().isEmpty()) {
                String[] startLineTokens = startLine.split(" ", 3);
                if (startLineTokens.length == 3) {
                    if (startLineTokens[0].equals("GET")) {
                        httpRequest = new HttpRequest();
                        // Метод
                        httpRequest.method = startLineTokens[0];
                        // Путь к запрашиваемому ресурсу
                        httpRequest.path = getPath(startLineTokens[1]);
                        // Параметры
                        httpRequest.params = getParams(startLineTokens[1]);
                        // Версия http
                        httpRequest.httpVersion = startLineTokens[2].substring("HTTP/".length());
                        // Заголовки
                        while (true) {
                            String headerLine = reader.readLine();
                            if (headerLine != null && !headerLine.isEmpty()) {
                                String[] headerLineTokens = headerLine.split(":", 2);
                                if (headerLineTokens.length == 2)
                                    httpRequest.headers.putIfAbsent(headerLineTokens[0].trim().toLowerCase(),
                                            headerLineTokens[1].trim().toLowerCase());
                            } else break;
                        }
                    }
                }
            }
            requestReceived = true;
        }
        return httpRequest;
    }

    private static String getPath(String rawPath) {
        String path = "";
        String[] pathToken = rawPath.split("\\?", 2);
        if (pathToken != null && pathToken[0].startsWith("/"))
            path = pathToken[0].substring(1);
        return path;
    }

    private static Map<String, String> getParams(String rawPath) {
        Map<String, String> params = new HashMap<>();
        String[] pathToken = rawPath.split("\\?", 2);
        if (pathToken.length == 2) {
            String[] paramsToken = pathToken[1].split("\\&");
            for (String paramEntry : paramsToken) {
                String[] paramsEntryToken = paramEntry.split("\\=");
                if (paramsEntryToken.length == 2)
                    params.putIfAbsent(paramsEntryToken[0], paramsEntryToken[1]);
            }
        }
        return params;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
