package Main.Handlers;

import Main.Messaging.Listener;
import Main.Messaging.Messenger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

abstract public class AbstractHandler implements Runnable, Messenger {

    private Socket clientSocket;

    private List<Listener> listeners = new ArrayList<>();

    public AbstractHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    abstract protected void session(InputStream is, OutputStream os) throws IOException;

    @Override
    final public void run() {
        if (clientSocket != null) {
            sendMessage(Thread.currentThread().getId(), 2); // сообщение о запуске нового обработчика
            try {
                session(clientSocket.getInputStream(), clientSocket.getOutputStream());
            }
            catch (IOException ex) {
            }
            finally {
                closeClientSocket();
                sendMessage(Thread.currentThread().getId(), 1); // сообщение о завершении работы обработчика
            }
        }
    }

    private void closeClientSocket()
    {
        if (clientSocket != null) {
            try {
                clientSocket.close();
            }
            catch (IOException ex)  {
            }
        }
    }

    @Override
    final public List<Listener> getListeners() {
        return listeners;
    }

}

