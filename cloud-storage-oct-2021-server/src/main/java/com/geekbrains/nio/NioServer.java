package com.geekbrains.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//ДЗ: создать обработчик команд
//    ls -> вывести список файлов и каталогов,
//    cd path-> перейти в нужный каталог,
//    cat file -> вывести на экран содержимое файла,
//    touch file -> создать пустой файл,
//    используя  данный сервер.

public class NioServer {

   private final Path root;
   private  ServerSocketChannel server;
   private Selector selector;
   private ByteBuffer buffer;

   public NioServer() throws Exception {

       root = Path.of("./");
       buffer = ByteBuffer.allocate(256);
       server = ServerSocketChannel.open(); //accept -> SocketChanel
       server.bind(new InetSocketAddress(8189));
       selector = Selector.open();
       server.configureBlocking(false); // работаем в неблокирующем режиме
       server.register(selector, SelectionKey.OP_ACCEPT); //регистрируем на выполнение операции accept

       while (server.isOpen()) {
           selector.select();
           Set<SelectionKey> selectedKeys = selector.selectedKeys();
           Iterator<SelectionKey> iterator = selectedKeys.iterator();
           while (iterator.hasNext()) {
               SelectionKey key = iterator.next();
               if (key.isAcceptable()) {
                   handleAccept(key);
               }
               if (key.isReadable()) {
                   handleRead(key);
               }
               iterator.remove();
           }
       }
   }

    private void handleRead(SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder sb = new StringBuilder();

        while (true) {

            int read = channel.read(buffer);
            if (read == -1) {
                channel.close();
                return;
            }
            if (read == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {

                sb.append((char) buffer.get());
            }
            buffer.clear();
        }
        String result = sb.toString().trim();
        if (result.toLowerCase().equals("ls")) {
            String fileList = Files.list(root)
                    .map(this::mapper)
                    .collect(Collectors.joining("\n")) + "\n";
            channel.write(ByteBuffer.wrap(fileList.getBytes(StandardCharsets.UTF_8)));
        } else {
            channel.write(ByteBuffer.wrap("Unknown command. \n\r".getBytes(StandardCharsets.UTF_8)));
        }
        if (result.toLowerCase().equals("cat".toLowerCase())) {

        }
        channel.write(ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8)));

    }

    private String mapper (Path path) {
       if (Files.isDirectory(path)) {
           return path.getFileName().toString() + " .........[ DIRECTORY]";
       } else {
           try {
               long size = Files.size(path);
               return path.getFileName().toString() + " ........[ FILE] " + size + " bytes";
           } catch (Exception e) {
               throw new RuntimeException("Path is not exists!");
           }
       }
    }

    private void handleAccept(SelectionKey key) throws Exception {

        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        channel.write(ByteBuffer.wrap("Welcome to terminal: \n\r".getBytes(StandardCharsets.UTF_8)));
    }

    public static void main(String[] args) throws Exception {
        new NioServer();
    }
}
