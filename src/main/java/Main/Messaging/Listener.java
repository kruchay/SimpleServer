package Main.Messaging;

public interface Listener {

    void receiveMessage(long source, int message);

}
