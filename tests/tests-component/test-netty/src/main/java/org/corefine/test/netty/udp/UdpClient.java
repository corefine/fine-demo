package org.corefine.test.netty.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * @author Fe by 2022/1/11 09:57
 */
public class UdpClient {

    public void test() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        byte[] buffer = new byte[4096];
        Thread t = new Thread(() -> {
            DatagramPacket packet = new DatagramPacket(buffer, 4096);
            while (true) {
                try {
                    socket.receive(packet);
                    System.out.println(new String(packet.getData(), 0, packet.getLength()));
                    packet.setLength(4096);
                    System.out.println();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        String data = "hi , ok!sadf";
        data += data;
        data += data;
        data += data;
        data += data;
        data += data;
        data += data;
        data += data;
        data += data;
        data += data;
        data += data;
        data += data;
        data += data;
        data += data;
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 9500);
        DatagramPacket dp_receive = new DatagramPacket(bytes, bytes.length, address);
        System.out.println("client is start!");
        System.out.println(data.length());
        while (true) {
            try {
                Thread.sleep(3000);
                socket.send(dp_receive);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new UdpClient().test();
    }
}
