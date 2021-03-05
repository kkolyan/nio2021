package com.nplekhanov.nio2021.rawchat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class RawChatClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8080);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        byte[] arrival = new byte[1024 * 1024];


        new Thread(() -> {
            try {
                while (true) {
                    int expected = in.readInt();
                    int n = in.read(arrival, 0, expected);
                    if (n < expected) {
                        throw new IllegalStateException("partial read not supported");
                    }
                    System.out.println(new String(arrival, 0, n, StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }).start();

        try {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
                out.writeInt(bytes.length);
                out.write(bytes);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
