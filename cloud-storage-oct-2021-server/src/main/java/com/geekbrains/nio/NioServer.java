package com.geekbrains.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;

//ДЗ: создать обработчик команд
//    ls -> вывести список файлов и каталогов,
//    cd path-> перейти в нужный каталог,
//    cat file -> вывести на экран содержимое файла,
//    touch file -> создать пустой файл,
//    используя  данный сервер.


@ApplicationScoped
public class NioServer  {

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

    public static String match(String glob, String location) throws IOException {
        StringBuilder result = new StringBuilder();
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
        Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(path)) {
                    result.append(path.toString());
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return result.toString();
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

        if (result.equalsIgnoreCase("ls")) {
           readDir(channel);
        } else {
            channel.write(ByteBuffer.wrap("Unknown command. \n\r".getBytes(StandardCharsets.UTF_8)));
        }
        if (result.equalsIgnoreCase("cat")) {
            channel.write(ByteBuffer.wrap("Please select a file to read. \n\r".getBytes(StandardCharsets.UTF_8)));

        }


//            try {
//
//                Files.lines(path)
//                        .filter(line -> line.startsWith(" "))
//                        .forEach(System.out::println);
//
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }


       // channel.write(ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8)))

    }

    private void readDir(SocketChannel channel) throws IOException {
        String fileList = null;
        try {
            fileList = Files.list(root)
                    .map(this::mapper)
                    .collect(Collectors.joining("\n")) + "\n";
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert fileList != null;
        channel.write(ByteBuffer.wrap(fileList.getBytes(StandardCharsets.UTF_8)));
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
