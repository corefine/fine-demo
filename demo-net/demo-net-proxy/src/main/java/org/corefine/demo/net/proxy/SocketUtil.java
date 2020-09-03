package org.corefine.demo.net.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Fe by 2020/9/3 11:16
 */
public class SocketUtil {

    public static void proxy(Socket client, Socket server, Runnable runnable) throws IOException {
        Thread readProxy = createThread(client, server, runnable);
        Thread writeProxy = createThread(server, client, runnable);
        readProxy.start();
        writeProxy.start();
    }

    private static Thread createThread(final Socket client, final Socket server, final Runnable runnable) throws IOException {
        final InputStream inputStream = client.getInputStream();
        final OutputStream outputStream = server.getOutputStream();
        return new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[8 * 1024];
                int index;
                try {
                    while ((index = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, index);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close(client, server, runnable);
                }
            }
        });
    }

    private static void close(Socket client, Socket server, Runnable runnable) {
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
        if (runnable != null) {
            runnable.run();
        }
    }
}
