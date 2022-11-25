package com.easychat.sse.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("message_record")
public class MsgRecordEntity {
    private String id;
    private String content;
    private String senderId;
    private String receiverId;
    private String receiverType;
    private String fileId;
    private LocalDateTime createTime;
}
