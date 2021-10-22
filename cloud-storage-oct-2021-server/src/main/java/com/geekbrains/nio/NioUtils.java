package com.geekbrains.nio;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class NioUtils {
    public static void main(String[] args) throws IOException {
        Path path = Path.of("root");
        if (path.getParent() != null) {
            path = path.getParent();
        }
        WatchService watchService = FileSystems.getDefault().newWatchService();
        new Thread(() -> {
            while (true) {
                WatchKey poll;
                try {
                    poll = watchService.take();
                    List<WatchEvent<?>> watchEvents = poll.pollEvents();
                    for (WatchEvent<?> watchEvent : watchEvents) {
                        System.out.println(watchEvent.context());
                        System.out.println(watchEvent.kind());
                    }
                    poll.reset();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        path.register(watchService, ENTRY_MODIFY, ENTRY_DELETE,  ENTRY_CREATE);
    }

}
