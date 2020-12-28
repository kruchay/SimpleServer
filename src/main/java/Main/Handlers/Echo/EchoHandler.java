package Main.Handlers.Echo;

import Main.Handlers.AbstractHandler;

import java.io.*;
import java.net.Socket;

public class EchoHandler extends AbstractHandler {

    public EchoHandler(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    protected void session(InputStream is, OutputStream os) {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            writer.write("Hello from server!");
            writer.newLine();
            boolean sessionFinished = false;
            while (!sessionFinished) {
                writer.write("Enter message > ");
                writer.flush();
                String inputMessage = reader.readLine();
                if (inputMessage == null) continue;
                if (inputMessage.trim().equalsIgnoreCase("bye")) {
                    sessionFinished = true;
                } else {
                    writer.write("Echo: " + inputMessage);
                    writer.newLine();
                    writer.flush();
                }
            };
        } catch (IOException ex) {
        }
   }

}
