package Main.Handlers.Http;

import Main.AppConfig;
import Main.Handlers.AbstractHandler;

import java.io.*;
import java.net.Socket;

public class HttpHandler extends AbstractHandler {

    public HttpHandler(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    protected void session(InputStream is, OutputStream os) throws IOException {
         try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
              BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
         {
            while (true) {
                HttpRequest request = HttpRequest.wait(reader);
                if (request != null) {
                    String response = executeRequest(request);
                    writer.write(response);
                    writer.flush();
                    // Проверка необходимости удерживания соединения (для http 1.1 соединения постоянные по умполчанию)
                    if ((!request.getHttpVersion().equals("1.1") &&
                         !request.getHeaders().getOrDefault("connection", "").contains("keep-Alive")) ||
                          request.getHeaders().getOrDefault("connection", "").contains("close"))
                        break;
                } else break;
            }
         }
    }

    private String executeRequest(HttpRequest request)
    {
        if (request.getMethod().equalsIgnoreCase("GET"))
            return executeGET(request);
        else return error(HTTP_ERROR.E501, request.getHttpVersion());
    }

    private String executeGET(HttpRequest request)
    {
         // Определение ресурса
        String requestPath = request.getPath().isEmpty() ? "index.html" : (request.getPath() + ((request.getPath().contains(".")) ? "" : ".html"));
        String resourcePath = AppConfig.getProperty("http_root_path", System.getProperty("user.dir")) + requestPath;

        File filePath = new File(resourcePath);
        if (!filePath.isFile())
          return error(HTTP_ERROR.E404, request.getHttpVersion());
        // Содержание страницы
        String content;
        try {
            content = getResourceContent(filePath);
        } catch (IOException ex) {
            return error(HTTP_ERROR.E488, request.getHttpVersion());
        }
        // Ответ клиенту по GET запросу
        StringBuilder responseBuilder = new StringBuilder(256 + content.length());
        responseBuilder.append("HTTP/");
        responseBuilder.append(request.getHttpVersion());
        responseBuilder.append(" 200 OK\r\n");
        responseBuilder.append("Content-Type: text/html\r\n");
        responseBuilder.append("Content-Length: ");
        responseBuilder.append(content.length());
        responseBuilder.append("\r\n\r\n");
        responseBuilder.append(content);
        return responseBuilder.toString();
    }

    private String getResourceContent(File filePath) throws IOException
    {
        long fileSize = filePath.length() / 2;
        if (fileSize > Integer.MAX_VALUE)
            throw new IOException("Too big file size");
        StringBuilder contentBuilder = new StringBuilder((int)fileSize);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
           String nextLine;
           while ((nextLine = reader.readLine()) != null) {
               contentBuilder.append(nextLine);
           }
        }
        return contentBuilder.toString();
    }

    private String error(HTTP_ERROR error, String httpVersion)
    {
        // Содержание страницы
        StringBuilder contentBuilder = new StringBuilder(256);
        contentBuilder.append("<html><title>Error ");
        contentBuilder.append(error.getDesc());
        contentBuilder.append("</title><body><h2>Error: <font color = #FF0000>");
        contentBuilder.append(error.getDesc());
        contentBuilder.append("</font></h2></body></html>");
        String content = contentBuilder.toString();

        // Ответ клиенту с указанием ошибки
        StringBuilder responseBuilder = new StringBuilder(256 + content.length());
        responseBuilder.append("HTTP/");
        responseBuilder.append(httpVersion);
        responseBuilder.append(" ");
        responseBuilder.append(error.getDesc());
        responseBuilder.append("\r\n");
        responseBuilder.append("Content-Type: text/html\r\n");
        responseBuilder.append("Content-Length: ");
        responseBuilder.append(content.length());
        responseBuilder.append("\r\n\r\n");
        responseBuilder.append(content);
        return responseBuilder.toString();
    }
}

