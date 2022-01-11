package org.corefine.test.netty.tcp;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author Fe by 2022/1/11 09:57
 */
public class TcpClient {

    public void test() throws IOException {
        Socket socket = new Socket("localhost", 9500);
        Thread t = new Thread(() -> {
            try {
//                System.out.println(socket.getInputStream().read());
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                System.out.println("reader is start!");
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
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
        data += "\nccc";
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        System.out.println("client is start!" + data.length());
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();
        while (true) {
            try {
                Thread.sleep(3000);
                outputStream.write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new TcpClient().test();
    }
}
