package com.xiaoban.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoban.server.dto.FamilyMessageVO;
import com.xiaoban.server.dto.MessageSendRequest;
import com.xiaoban.server.entity.FamilyMessage;
import com.xiaoban.server.entity.User;
import com.xiaoban.server.mapper.FamilyMessageMapper;
import com.xiaoban.server.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyMessageService {

    private final FamilyMessageMapper familyMessageMapper;
    private final UserMapper userMapper;
    private final PushService pushService;

    public FamilyMessage send(Long senderId, MessageSendRequest req) {
        if (!StringUtils.hasText(req.getMsgType())) {
            req.setMsgType("text");
        }
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
        pushService.pushToUser(
                req.getReceiverId(),
                "小伴消息",
                "您有一条新消息",
                Map.of("type", "family_msg"));
        return msg;
    }

    public List<FamilyMessageVO> received(Long userId, int page, int size) {
        List<FamilyMessage> records = familyMessageMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<FamilyMessage>()
                        .eq(FamilyMessage::getReceiverId, userId)
                        .orderByDesc(FamilyMessage::getCreatedAt)
        ).getRecords();

        Set<Long> senderIds = records.stream()
                .map(FamilyMessage::getSenderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> senderNames = senderIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(senderIds).stream()
                        .collect(Collectors.toMap(User::getUserId, this::resolveDisplayName, (a, b) -> a));

        return records.stream()
                .map(message -> FamilyMessageVO.from(message, senderNames.get(message.getSenderId())))
                .toList();
    }

    public void markRead(Long id, Long userId) {
        FamilyMessage msg = familyMessageMapper.selectById(id);
        if (msg != null && userId.equals(msg.getReceiverId())) {
            msg.setIsRead(1);
            familyMessageMapper.updateById(msg);
        }
    }

    private String resolveDisplayName(User user) {
        if (user == null) {
            return "家人";
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname();
        }
        if (StringUtils.hasText(user.getPhone()) && user.getPhone().length() >= 4) {
            String phone = user.getPhone();
            return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
        }
        return "家人";
    }
}
