package com.xiaoban.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoban.server.common.BusinessException;
import com.xiaoban.server.common.ResultCode;
import com.xiaoban.server.dto.FamilyMessageVO;
import com.xiaoban.server.dto.MessageSendRequest;
import com.xiaoban.server.entity.BindingRelation;
import com.xiaoban.server.entity.FamilyMessage;
import com.xiaoban.server.entity.User;
import com.xiaoban.server.mapper.BindingRelationMapper;
import com.xiaoban.server.mapper.FamilyMessageMapper;
import com.xiaoban.server.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
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
    private final BindingRelationMapper bindingRelationMapper;
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

    public List<FamilyMessageVO> familyGroup(Long elderId, int page, int size) {
        User user = userMapper.selectById(elderId);
        if (user == null || !"elder".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        List<BindingRelation> activeRelations = activeFamilyRelations(elderId);
        List<Long> familyIds = new ArrayList<>();
        familyIds.add(elderId);
        activeRelations.stream()
                .map(BindingRelation::getChildId)
                .filter(Objects::nonNull)
                .forEach(familyIds::add);

        if (familyIds.size() <= 1) {
            return List.of();
        }

        int querySize = Math.max(size * Math.max(2, activeRelations.size()), size);
        List<FamilyMessage> records = familyMessageMapper.selectPage(
                new Page<>(page, querySize),
                new LambdaQueryWrapper<FamilyMessage>()
                        .in(FamilyMessage::getSenderId, familyIds)
                        .in(FamilyMessage::getReceiverId, familyIds)
                        .orderByDesc(FamilyMessage::getCreatedAt)
                        .orderByDesc(FamilyMessage::getId)
        ).getRecords();

        List<FamilyMessage> visibleRecords = collapseElderBroadcastDuplicates(records, elderId, size);
        Set<Long> senderIds = visibleRecords.stream()
                .map(FamilyMessage::getSenderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> senderNames = senderIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(senderIds).stream()
                        .collect(Collectors.toMap(User::getUserId, this::resolveDisplayName, (a, b) -> a));

        return visibleRecords.stream()
                .map(message -> FamilyMessageVO.from(message, senderNames.get(message.getSenderId())))
                .toList();
    }

    public void sendFamilyGroup(Long elderId, MessageSendRequest req) {
        User user = userMapper.selectById(elderId);
        if (user == null || !"elder".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        List<BindingRelation> activeRelations = activeFamilyRelations(elderId);
        if (activeRelations.isEmpty()) {
            throw new BusinessException(4006, "暂未绑定家人，无法发送");
        }
        if (!StringUtils.hasText(req.getMsgType())) {
            req.setMsgType("text");
        }

        for (BindingRelation relation : activeRelations) {
            if (relation.getChildId() == null) {
                continue;
            }
            FamilyMessage msg = FamilyMessage.builder()
                    .senderId(elderId)
                    .receiverId(relation.getChildId())
                    .msgType(req.getMsgType())
                    .content(req.getContent())
                    .mediaUrl(req.getMediaUrl())
                    .duration(req.getDuration())
                    .isRead(0)
                    .build();
            familyMessageMapper.insert(msg);
            pushService.pushToUser(
                    relation.getChildId(),
                    "小伴消息",
                    "家庭聊天群有一条新消息",
                    Map.of("type", "family_msg"));
        }
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

    private List<BindingRelation> activeFamilyRelations(Long elderId) {
        return bindingRelationMapper.selectList(
                new LambdaQueryWrapper<BindingRelation>()
                        .eq(BindingRelation::getElderId, elderId)
                        .eq(BindingRelation::getStatus, "active")
        );
    }

    private List<FamilyMessage> collapseElderBroadcastDuplicates(List<FamilyMessage> records,
                                                                 Long elderId,
                                                                 int maxSize) {
        List<FamilyMessage> result = new ArrayList<>();
        Set<String> seenBroadcasts = new HashSet<>();
        for (FamilyMessage message : records) {
            boolean isElderBroadcast = elderId.equals(message.getSenderId());
            if (isElderBroadcast) {
                String key = message.getSenderId() + "|"
                        + Objects.toString(message.getMsgType(), "") + "|"
                        + Objects.toString(message.getContent(), "") + "|"
                        + Objects.toString(message.getMediaUrl(), "") + "|"
                        + Objects.toString(message.getDuration(), "") + "|"
                        + Objects.toString(message.getCreatedAt(), "");
                if (!seenBroadcasts.add(key)) {
                    continue;
                }
            }
            result.add(message);
            if (result.size() >= maxSize) {
                break;
            }
        }
        return result;
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
