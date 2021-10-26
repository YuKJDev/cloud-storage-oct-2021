package com.geekbrains.common.messages;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public abstract class AMessage implements IMessage{
    @Getter
    private String id = UUID.randomUUID().toString();

    @Getter
    @Setter
    private String conversationId;
}
