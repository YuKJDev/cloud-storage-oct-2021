package com.geekbrains.nio;

import java.nio.ByteBuffer;

public class Buffers {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(5); // не можем превышать значение размера буфера

        //пишем в буфер
        buffer.put((byte) 'a');
        buffer.put((byte) 'b');
        buffer.put((byte) 'c');
        buffer.put((byte) 'd');

        buffer.flip();

        //Читаем из буфера
        while (buffer.hasRemaining()) {
            System.out.println((char) buffer.get());
        }
    }
}
