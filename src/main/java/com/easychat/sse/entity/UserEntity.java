package com.easychat.sse.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class UserEntity {
    private String id;
    private String userName;
    private String avatar;
    private LocalTime loginTime;
    private String lastPush;
    private Map<String, List<MessageEntity>> recentConversationRecord = new ConcurrentHashMap<>();

    public void recordMsg(String receiverId, MessageEntity messageEntity) {
        List<MessageEntity> messageEntityList = recentConversationRecord.computeIfAbsent(receiverId, (s) -> new ArrayList<>());
        if (messageEntityList.size() > 50) {
            messageEntityList.remove(0);
        }
        messageEntityList.add(messageEntity);
    }

    public List<MessageEntity> findRecords(String receiverId) {
        return recentConversationRecord.getOrDefault(receiverId, Collections.emptyList());
    }

    public UserEntity(String id, String userName, String avatar, LocalTime loginTime, String lastPush) {
        this.id = id;
        this.userName = userName;
        this.avatar = avatar;
        this.loginTime = loginTime;
        this.lastPush = lastPush;
    }
}
