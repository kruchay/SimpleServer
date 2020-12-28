package Main;

import Main.Server.Server;

public class AppEntryPoint {
    public static void main(String[] args) {
        Server.getInstance().start();
    }
}
