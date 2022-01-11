package org.corefine.test.net;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * @author Fe by 2022/1/11 09:57
 */
public class UdpTest {

    public void test() throws IOException {
        DatagramSocket socket = new DatagramSocket(9500);

        byte[] buf = new byte[1024 * 64];
        DatagramPacket dp_receive = new DatagramPacket(buf, 4096 * 4);
        System.out.println("server is on，waiting for client to send data......");
        boolean f = true;
        while(f) {
            socket.receive(dp_receive);
            String data = new String(dp_receive.getData(),0,dp_receive.getLength());
            System.out.println(data.length());
            System.out.println(data);
            dp_receive.setLength(buf.length);
//            send.setData(("rec@" + data.length()).getBytes(StandardCharsets.UTF_8));
        }
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        new UdpTest().test();
    }
}
