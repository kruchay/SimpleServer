package Main.Server;

import Main.AppConfig;
import Main.Handlers.AbstractHandler;
import Main.Handlers.Echo.EchoHandler;
import Main.Handlers.Http.HttpHandler;
import Main.Messaging.Listener;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;

public class Server implements Listener {

    private static Server instance = null;
    private ServerSocket serverSocket;
    private int max_connections;
    private int server_port;
    private int serverSocketTimeout;
    private int clientSocketTimeout;
    private Function<Socket, ? extends AbstractHandler> handlerFactory;
    private volatile int active_connections;
    private volatile boolean running = false;

    private Server() {
        max_connections = AppConfig.getProperty("max_connections", 10);
        active_connections = 0;
        server_port = AppConfig.getProperty("server_port", 8080);
        serverSocketTimeout = AppConfig.getProperty("server_socket_timeout", 2000);
        clientSocketTimeout = AppConfig.getProperty("client_socket_timeout", 2000);
        handlerFactory = (AppConfig.getProperty("type", "HTTP")
                .equalsIgnoreCase("ECHO")) ? EchoHandler::new : HttpHandler::new;
    }

    public static Server getInstance() {
        if (instance == null)
            instance = new Server();
        return instance;
    }

    public void start() {

        // Запуск потока для сервисного управления сервером (например через Telnet)
        Thread serviceThread = new Thread(ServiceManagement::start);
        serviceThread.setDaemon(true);
        serviceThread.start();

        // Открытие серверного сокета
        try {
            if (serverSocket == null) {
                serverSocket = new ServerSocket(server_port, max_connections);
                serverSocket.setSoTimeout(serverSocketTimeout); // делаем сокет прерываемый, чтоб не зависал метод accept и мы могли из сервисного потока влиять на свойство running
            }
        }
        catch (IOException ex) {
            System.err.println("ERROR! Cannot open server socket");
            closeServerSocket();
            return;
        }

        // Открытие получилось, запускаем цикл сервера
        System.out.printf("Server is started... (address: %s, port: %d)\r\n",
                getAvailableIP().stream().reduce((a1, a2) -> a1 + " | " + a2).orElse("?.?.?.?"),
                serverSocket.getLocalPort());
        running = true;
        updateStatistic();

       // Главный цикл сервера
        while (running) {
            try {
                // Ожидание подключения
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(clientSocketTimeout);
                // Запуск обработчика в новом потоке
                AbstractHandler handler = handlerFactory.apply(clientSocket);
                handler.bindListener(this);
                Thread handlerThread = new Thread(handler);
                handlerThread.setDaemon(true);
                handlerThread.start();
            }
            //catch (SocketTimeoutException ex) {}
            catch (IOException ex) {}
        }
        closeServerSocket();
        System.out.println("\r\nServer was stopped");
    }

    public void stop() {
        running = false;
    }

    private void closeServerSocket()  {
       if (serverSocket != null) {
           try {
               serverSocket.close();
           }
           catch (IOException ex) {}
           finally {
               serverSocket = null;
           }
       }
    }

    @Override
    synchronized public void receiveMessage(long source, int message) {
        // Сообщение о завершении работы обработчика
        if (message == 1) {
            --active_connections;
        }
        // Сообщение о запуске нового обработчика
        if (message == 2) {
            ++active_connections;
        }
        updateStatistic();
    }

    private void updateStatistic() {
       System.out.printf("\ractive connections: %d", active_connections);
    }

    private List<String> getAvailableIP() {
        ArrayList<String> availableIP = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress instanceof Inet4Address) {
                            availableIP.add(inetAddress.getHostAddress());
                        }
                    }
                }
            }
        } catch (SocketException ex) {

        }
        return availableIP;
    }

}
