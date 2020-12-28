package Main.Server;

import Main.AppConfig;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServiceManagement {

    private static ServerSocket serviceSocket = null;
    private static int service_port = AppConfig.getProperty("service_port", 8081);

    private ServiceManagement() {
    }

    static void start() {
        // Открытие серверного сокета
        try {
            if (serviceSocket == null) {
                serviceSocket = new ServerSocket(service_port, 1);
                serviceSocket.setSoTimeout(15000);
            }
        }
        catch (IOException ex) {
            System.err.println("ERROR! Cannot open socket for service management");
            closeServiceSocket();
            return;
        }

        while (true)
        {
            Socket clientServiceSocket = null;
            try {
                clientServiceSocket = serviceSocket.accept();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientServiceSocket.getOutputStream()));
                     BufferedReader reader = new BufferedReader(new InputStreamReader(clientServiceSocket.getInputStream()))) {
                    writer.write("Hello from server!");
                    writer.write(System.lineSeparator());
                    while (true) {
                        writer.write("> ");
                        writer.flush();
                        String inputCommand = reader.readLine();
                        if (inputCommand != null && inputCommand.replaceAll("[^a-zA-Z]", "")
                                .equalsIgnoreCase("shutdown")) {
                            Server.getInstance().stop();
                            closeClientServiceSocket(clientServiceSocket);
                            closeServiceSocket();
                            return;
                        } else {
                            writer.write("Unknown command");
                            writer.write(System.lineSeparator());
                            writer.flush();
                        }
                    }
                }
                catch (IOException ex) {}
            }
            catch (IOException ex) {}
            finally {
                closeClientServiceSocket(clientServiceSocket);
            }
        }
    }

    private static void closeServiceSocket() {
        if (serviceSocket != null) {
            try {
                serviceSocket.close();
            }
            catch (IOException ex) {}
            finally {
                serviceSocket = null;
            }
        }
    }

    private static void closeClientServiceSocket(Socket clientSocket) {
        if (clientSocket != null) {
            try {
                clientSocket.close();
            }
            catch (IOException ex) {}
            finally {
                clientSocket = null;
            }
        }
    }
}
