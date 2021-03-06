package com.easychat.sse.controller;


import com.easychat.sse.dao.MockUserDao;
import com.easychat.sse.model.entity.MessageAndUsers;
import com.easychat.sse.model.entity.MessageEntity;
import com.easychat.sse.model.entity.OldUserEntity;
import com.easychat.sse.enums.EventType;
import com.easychat.sse.event.UserOnlineEvent;
import com.easychat.sse.exception.CustomRuntimeException;
import com.easychat.sse.server.SseEmitterServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.easychat.sse.shiro.ShiroUtil.getUserId;


@RestController
@Slf4j
@CrossOrigin
public class FluxController {

    @Resource
    ObjectMapper objectMapper;

    @GetMapping(value = "/connect")
    public SseEmitter streamFlux() {
        return SseEmitterServer.connect(getUserId());
    }

    /**
     * 推送给所有人
     *
     * @param message 消息
     * @return ResponseBody
     */
    @GetMapping("/push")
    public ResponseEntity<String> push(@RequestParam String message) {
        SseEmitterServer.batchSendMessage(message);
        return ResponseEntity.ok("WebSocket 推送消息给所有人");
    }

    @Async
    @EventListener(UserOnlineEvent.class)
    public void active(UserOnlineEvent event) throws JsonProcessingException {
        MessageEntity message = MessageEntity.builder()
                .createUserId(event.getUserId())
                .content(null)
                .sendTime(LocalDateTime.now())
                .messageType(EventType.CONNECT.getType())
                .build();
        MessageAndUsers messageAndUsers = MessageAndUsers.builder().message(message)
                .onlineUsers(new ArrayList<>(MockUserDao.userMap.values()))
                .build();
        String msg = objectMapper.writeValueAsString(messageAndUsers);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new CustomRuntimeException("线程终止异常：" + e.getMessage(), e);
        }
        SseEmitterServer.batchSendMessage(msg);
    }

    @GetMapping("/close/{id}")
    public void close(@PathVariable String id) throws JsonProcessingException {
        SseEmitterServer.removeUser(id);
        MockUserDao.userMap.remove(id);
        MessageEntity message = MessageEntity.builder()
                .createUserId(id)
                .content(null)
                .sendTime(LocalDateTime.now())
                .messageType(EventType.CLOSE.getType())
                .build();
        MessageAndUsers messageAndUsers = MessageAndUsers.builder().message(message).onlineUsers(Collections.emptyList()).build();
        String msg = objectMapper.writeValueAsString(messageAndUsers);
        SseEmitterServer.batchSendMessage(msg);
    }


    /**
     * 发送给单个人
     *
     * @param message 信息
     * @param id      用户ID
     */
    @GetMapping("/pushTo")
    public void pushOne(@RequestParam String message, @RequestParam String id, @RequestParam String sender) throws JsonProcessingException {
        OldUserEntity speaker = MockUserDao.requireThrow(sender, "当前用户不存在，请重新登录");
        OldUserEntity receiver = MockUserDao.requireThrow(id, "消息接收人不存在，请刷新页面");
        speaker.setLastPush(message);

        MessageEntity messageEntity = MessageEntity.builder()
                .createUserId(sender)
                .content(message)
                .sendTime(LocalDateTime.now())
                .messageType(EventType.PUSH_MESSAGE.getType())
                .build();
        MessageAndUsers messageAndUsers = MessageAndUsers.builder().message(messageEntity)
                .onlineUsers(new ArrayList<>(MockUserDao.userMap.values()))
                .build();
        speaker.recordMsg(id, messageEntity);
        receiver.recordMsg(sender, messageEntity);
        String msg = objectMapper.writeValueAsString(messageAndUsers);
        SseEmitterServer.sendMessage(id, msg);
    }

    @GetMapping("/findRecord")
    public List<MessageEntity> findRecord(@RequestParam String choseId,@RequestParam String userId){
        return MockUserDao.require(userId).findRecords(choseId);
    }

}
