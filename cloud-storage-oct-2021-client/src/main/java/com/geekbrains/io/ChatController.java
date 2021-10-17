package com.geekbrains.io;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ChatController implements Initializable {

    public ListView<String> listView;
    public TextField input;
    public Button send;
    private DataInputStream dis;
    private DataOutputStream dos;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String message = dis.readUTF();
                        Platform.runLater(() -> listView.getItems().add(message));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(ActionEvent actionEvent) throws IOException {
        String message = input.getText();
        dos.writeUTF(message);
        dos.flush();
        input.clear();
    }

    private void sendFile(String fileName) {
        try {

            dos.writeUTF("upload");
            dos.writeUTF(fileName);
            File file = new File("client" + File.separator +"root" + File.separator + fileName);
            if (!file.exists()) {
                System.out.println("File is not exist");
                dos.writeUTF("File is not exist");
            }
            long length = file.length();
            dos.writeLong(length);
            FileInputStream fileBytes = new FileInputStream(file);
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = fileBytes.read(buffer)) != -1) {
                dos.write(buffer, 0, read);
            }
            dos.flush();
            System.out.println(dis.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getFile(String fileName) {
        try{

            dos.writeUTF("download");
            dos.writeUTF(fileName);
            File file = new File("client" + File.separator +"root" + File.separator + fileName);
            if (file.exists()) { file.delete();}
            file.createNewFile();
            long size = dis.readLong();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            for (int i = 0; i < (size + buffer.length - 1) / 1014; i++) {
                int read = dis.read(buffer);
                fos.write(buffer, 0, read);
            }
            System.out.println(dis.readUTF());
            fos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    public void actionSend (){
      send.setOnAction((a -> {
          String[] cmd = input.getText().split(" ");
          if (cmd[0].equals("upload")) {
              sendFile(cmd[1]);
          }
          if (cmd[0].equals("download")) {
              getFile(cmd[1]);
          }
          else {
              try {
                  sendMessage(a);
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
          input.setText("");

      }));

    }


}



