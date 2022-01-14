package org.corefine.demo.net.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Fe by 2020/8/26 18:20
 */
public class SocketServer2 {

    public void service() {
        int port = 9500;

        final ServerSocket server;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("start server: " + port);
        while (!server.isClosed()) {
            try {
                proxy(server.accept(), new Socket("dmo-lego-apaas-providence-eye-rest", 9500));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void proxy(Socket client, Socket server) throws IOException {
        Thread readProxy = createThread(client, server);
        Thread writeProxy = createThread(server, client);
        readProxy.start();
        writeProxy.start();
    }

    private static Thread createThread(final Socket client, final Socket server) throws IOException {
        final InputStream inputStream = client.getInputStream();
        final OutputStream outputStream = server.getOutputStream();
        return new Thread(() -> {
            byte[] buffer = new byte[8 * 1024];
            int index;
            try {
                while ((index = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, index);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(client, server);
            }
        });
    }

    private static void close(Socket client, Socket server) {
        if (client != null) {
            try {
                client.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (server != null) {
            try {
                server.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws IOException {
        new SocketServer2().service();
    }
}
