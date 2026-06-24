package com.xiaoban.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoban.server.dto.MessageSendRequest;
import com.xiaoban.server.entity.FamilyMessage;
import com.xiaoban.server.mapper.FamilyMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyMessageService {

    private final FamilyMessageMapper familyMessageMapper;
    private final PushService pushService;

    public FamilyMessage send(Long senderId, MessageSendRequest req) {
        FamilyMessage msg = FamilyMessage.builder()
                .senderId(senderId)
                .receiverId(req.getReceiverId())
                .msgType(req.getMsgType())
                .content(req.getContent())
                .mediaUrl(req.getMediaUrl())
                .duration(req.getDuration())
                .isRead(0)
                .build();
        familyMessageMapper.insert(msg);
        pushService.pushToUser(req.getReceiverId(), "小伴消息", "您有一条新消息", null);
        return msg;
    }

    public List<FamilyMessage> received(Long userId, int page, int size) {
        return familyMessageMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<FamilyMessage>()
                        .eq(FamilyMessage::getReceiverId, userId)
                        .orderByDesc(FamilyMessage::getCreatedAt)
        ).getRecords();
    }

    public void markRead(Long id) {
        FamilyMessage msg = familyMessageMapper.selectById(id);
        if (msg != null) {
            msg.setIsRead(1);
            familyMessageMapper.updateById(msg);
        }
    }
}
