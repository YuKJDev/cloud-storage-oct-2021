package com.geekbrains.common.messages;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public interface IMessage extends Serializable {
    @NotNull
    String getId();

    @NotNull
    String getConversationId();

    void setConversationId(@NotNull String conversationId);

}
