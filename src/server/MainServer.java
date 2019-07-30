package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainServer {
    private static Logger logger= Logger.getLogger(MainServer.class.getName());

    public static void main(String[] args) {
        while(true) {
            try (ServerSocket serverSocketT = new ServerSocket(45777);
                 ServerSocket serverSocketF = new ServerSocket(45778)) {
                Socket socketText = serverSocketT.accept();
                Socket socketFile = serverSocketF.accept();
                new Thread(new Server(socketText, socketFile)).start();
            } catch (IOException e) {
                logger.log(Level.ALL, "Ошибка соединения!\n" + e.getStackTrace());
            }
        }

    }
}
