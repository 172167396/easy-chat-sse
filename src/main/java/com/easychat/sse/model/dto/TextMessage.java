package com.easychat.sse.model.dto;

import com.easychat.sse.enums.MessageType;
import com.easychat.sse.enums.RecentMsgType;
import com.easychat.sse.model.domain.SseMessage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TextMessage implements SseMessage {
    private String content;
    private String createTime;
    private String sender;
    private String name;
    private String senderAvatar;
    private String lastActiveTime;
    private RecentMsgType type;
    @JsonIgnore
    private String receiver;

    @Override
    public MessageType getMessageType() {
        return MessageType.TEXT;
    }

    @Override
    public RecentMsgType getType() {
        return type;
    }

    @Override
    public String getReceiver() {
        return receiver;
    }

    @Override
    public String getFileId() {
        return null;
    }
}
