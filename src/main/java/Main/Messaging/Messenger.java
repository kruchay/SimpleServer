package Main.Messaging;

import java.util.List;

public interface Messenger {

    List<Listener> getListeners();

    default void bindListener(Listener listener) {
        if (listener != null)
            getListeners().add(listener);
    }

    default void unbindListener(Listener listener) {
        getListeners().removeIf(l -> l==listener);
    }

    default void sendMessage(long source, int message) {
        getListeners().forEach(l -> l.receiveMessage(source, message));
    }

}
