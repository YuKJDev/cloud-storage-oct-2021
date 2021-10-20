package com.geekbrains.io;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ChatController implements Initializable {

    private static final int BUFFER_SIZE = 1024;

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

            File file = new File(File.separator +"root" + File.separator + fileName);
            if (!file.exists()) {
                System.out.println("File is not exist");
                dos.writeUTF("File is not exist");
            }
            long length = file.length();
            dos.writeLong(length);
            FileInputStream fileBytes = new FileInputStream(file);
            int read;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((read = fileBytes.read(buffer)) != -1) {
                dos.write(buffer, 0, read);
            }
            dos.flush();
            System.out.println(dis.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getFile(String fileName){

        try{
            dos.writeUTF("download");
            dos.writeUTF(fileName);
            File file = new File( File.separator +"root" + File.separator + fileName);
            if (file.exists()) file.delete();
            file.createNewFile();

            long size = dis.readLong();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            for (int i = 0; i < (size + buffer.length - 1) / BUFFER_SIZE; i++) {
                int read = dis.read(buffer);
                fos.write(buffer, 0, read);
            }
            System.out.println(dis.readUTF());
            fos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    public void actionSend () {
        input.setOnAction((this::handle));

        }

    private void handle(ActionEvent a) {

        final Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);

        Label exitLabel = new Label("Check a file");
        exitLabel.setAlignment(Pos.BASELINE_CENTER);
        Button yesBtn = new Button("Open");
        yesBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                String[] cmd = input.getText().toLowerCase().split(" ");

                if (cmd[0].equals("upload")) {
                    sendFile(cmd[1]);
                }
                if (cmd[0].equals("download")) {
                    getFile(cmd[1]);
                } else {
                    try {
                        sendMessage(a);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                input.setText("");

            }
        });
        Button noBtn = new Button("Cancel");
        noBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                dialogStage.close();

            }

        });

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.BASELINE_CENTER);
        hBox.setSpacing(40.0);
        hBox.getChildren().addAll(yesBtn, noBtn);

        VBox vBox = new VBox();
        vBox.setSpacing(40.0);
        vBox.getChildren().addAll(exitLabel, hBox);

        dialogStage.setScene(new Scene(vBox));
        dialogStage.show();


    }
}






