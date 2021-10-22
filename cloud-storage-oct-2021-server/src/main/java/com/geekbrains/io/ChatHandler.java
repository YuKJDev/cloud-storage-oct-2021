package com.geekbrains.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatHandler implements Runnable {

    private static int counter = 0;
    private final String userName;
    private Path clientDir;
    private final Path root;
    private final Server server;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final SimpleDateFormat format;
    private final int BUFFER_SIZE = 1024;
    byte[] buffer;

    public ChatHandler(Socket socket, Server server) throws Exception {
        this.server = server;
        buffer = new byte[BUFFER_SIZE];
        root = Paths.get("server_root");
        if (!Files.exists(root)) {
            Files.createDirectory(root);
        }
        counter++;
        userName = "User_" + counter;
        clientDir = root.resolve(userName);
        format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                String fileName = dis.readUTF();
                long size = dis.readLong();
                String msg = dis.readUTF();
                Path path = clientDir.resolve(fileName);
                try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
                    for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                        int read = dis.read(buffer);
                        fos.write(buffer, 0, read);
                        
                    }
                }
                  responseOk();
                //server.broadCastMessage(getMessage(msg));

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Connection was broken");

        }
    }



    private void responseOk() throws Exception {
        dos.writeUTF("File received!");
        dos.flush();
    }

    public String getMessage(String msg) {
        return getTime() + " [" + userName + "]: " + msg;
    }

    public String getTime() {
        return format.format(new Date());
    }

    public void sendMessage(String msg) throws Exception {
        dos.writeUTF(msg);
        dos.flush();
    }
}
