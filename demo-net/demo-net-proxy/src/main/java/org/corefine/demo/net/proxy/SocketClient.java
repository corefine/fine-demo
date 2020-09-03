package org.corefine.demo.net.proxy;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Fe by 2020/8/26 18:20
 */
public class SocketClient {
    private Socket manager, dest;

    public void service() throws IOException {
        final String clientName = "testName";
        //启动client时指定目标地址
        String destIp = "172.16.0.41";
        int destPort = 3306;

        //manager地址
        String managerIp = "192.168.2.210";
        int managerPort = 6634;

        manager = new Socket(managerIp, managerPort);
        System.out.println("manager is connect");
        dest = new Socket(destIp, destPort);
        System.out.println("dest is connect");
        SocketUtil.proxy(manager, dest, new Runnable() {
            @Override
            public void run() {
                System.out.println("close");
            }
        });
    }

    public static void main(String[] args) throws IOException {
        new SocketClient().service();
    }
}
