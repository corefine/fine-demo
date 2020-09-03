package org.corefine.demo.net.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Fe by 2020/8/26 18:20
 */
public class SocketServer {
    private volatile Socket destProxy, clientProxy;
    private final Object LOCK = new Object();

    public void service() {
        int port = 6634;

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
                Socket socket = server.accept();
                synchronized (LOCK) {
                    if (destProxy == null) {
                        destProxy = socket;
                    } else if (clientProxy == null) {
                        clientProxy = socket;
                        SocketUtil.proxy(clientProxy, destProxy, new Runnable() {
                            @Override
                            public void run() {
                                synchronized (LOCK) {
                                    if (destProxy != null) {
                                        try {
                                            destProxy.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        destProxy = null;
                                    }
                                    if (clientProxy != null) {
                                        try {
                                            clientProxy.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        clientProxy = null;
                                    }
                                    System.out.println("connect reset");
                                }
                            }
                        });
                    } else {
                        System.out.println("connect is too much");
                        socket.getOutputStream().write("connect is too much".getBytes());
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        socket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new SocketServer().service();
    }
}
