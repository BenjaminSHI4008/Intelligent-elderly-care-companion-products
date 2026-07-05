package com.xiaoban.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
<<<<<<< HEAD
import com.xiaoban.server.common.BusinessException;
import com.xiaoban.server.common.ResultCode;
import com.xiaoban.server.dto.ConversationSummaryVO;
import com.xiaoban.server.dto.FamilyGroupMemberVO;
import com.xiaoban.server.dto.FamilyMessageVO;
import com.xiaoban.server.dto.MessageSendRequest;
import com.xiaoban.server.entity.BindingRelation;
import com.xiaoban.server.entity.FamilyMessage;
import com.xiaoban.server.entity.User;
import com.xiaoban.server.mapper.BindingRelationMapper;
=======
import com.xiaoban.server.dto.FamilyMessageVO;
import com.xiaoban.server.dto.MessageSendRequest;
import com.xiaoban.server.entity.FamilyMessage;
import com.xiaoban.server.entity.User;
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
import com.xiaoban.server.mapper.FamilyMessageMapper;
import com.xiaoban.server.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

<<<<<<< HEAD
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
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
<<<<<<< HEAD
    private final BindingRelationMapper bindingRelationMapper;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
    private final PushService pushService;

    public FamilyMessage send(Long senderId, MessageSendRequest req) {
        if (!StringUtils.hasText(req.getMsgType())) {
            req.setMsgType("text");
        }
<<<<<<< HEAD
        assertCanMessagePeer(senderId, req.getReceiverId());

=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
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

<<<<<<< HEAD
    public List<FamilyMessageVO> familyGroup(Long userId, Long elderId, int page, int size, boolean ascending) {
        Long resolvedElderId = resolveElderIdForFamilyGroup(userId, elderId);
        return getFamilyGroupMessages(resolvedElderId, page, size, ascending);
    }

    public List<FamilyGroupMemberVO> familyGroupMembers(Long userId, Long elderId) {
        Long resolvedElderId = resolveElderIdForFamilyGroup(userId, elderId);
        List<FamilyGroupMemberVO> members = new ArrayList<>();

        User elder = userMapper.selectById(resolvedElderId);
        if (elder != null) {
            members.add(FamilyGroupMemberVO.builder()
                    .userId(elder.getUserId())
                    .nickname(resolveDisplayName(elder))
                    .role("elder")
                    .elder(true)
                    .build());
        }

        for (BindingRelation relation : activeFamilyRelations(resolvedElderId)) {
            if (relation.getChildId() == null) {
                continue;
            }
            User child = userMapper.selectById(relation.getChildId());
            if (child == null) {
                continue;
            }
            members.add(FamilyGroupMemberVO.builder()
                    .userId(child.getUserId())
                    .nickname(resolveDisplayName(child))
                    .role("child")
                    .elder(false)
                    .build());
        }
        return members;
    }

    public List<ConversationSummaryVO> listConversations(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !"child".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        List<BindingRelation> relations = bindingRelationMapper.selectList(
                new LambdaQueryWrapper<BindingRelation>()
                        .eq(BindingRelation::getChildId, userId)
                        .eq(BindingRelation::getStatus, "active")
                        .orderByDesc(BindingRelation::getBindTime));

        List<ConversationSummaryVO> summaries = new ArrayList<>();
        for (BindingRelation relation : relations) {
            Long elderId = relation.getElderId();
            if (elderId == null) {
                continue;
            }
            User elder = userMapper.selectById(elderId);
            if (elder == null) {
                continue;
            }

            List<Long> familyIds = resolveFamilyIds(elderId);
            FamilyMessage latest = findLatestFamilyMessage(familyIds);
            int unreadCount = countFamilyUnread(userId, familyIds);

            summaries.add(ConversationSummaryVO.builder()
                    .peerId(elderId)
                    .peerName(elder.getNickname())
                    .lastMsgType(latest != null ? latest.getMsgType() : "text")
                    .lastContent(formatGroupPreview(latest, familyIds))
                    .lastTime(latest != null ? latest.getCreatedAt() : relation.getBindTime())
                    .unreadCount(unreadCount)
                    .muted(false)
                    .build());
        }

        summaries.sort(Comparator.comparing(
                ConversationSummaryVO::getLastTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return summaries;
    }

    public List<FamilyMessageVO> conversation(Long userId, Long peerId, int page, int size) {
        User user = userMapper.selectById(userId);
        if (user != null && "child".equals(user.getRole())) {
            User peer = userMapper.selectById(peerId);
            if (peer != null && "elder".equals(peer.getRole())) {
                return getFamilyGroupMessages(peerId, page, size, true);
            }
        }
        assertCanMessagePeer(userId, peerId);

        List<FamilyMessage> records = familyMessageMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<FamilyMessage>()
                        .and(wrapper -> wrapper
                                .nested(w -> w.eq(FamilyMessage::getSenderId, userId)
                                        .eq(FamilyMessage::getReceiverId, peerId))
                                .or(w -> w.eq(FamilyMessage::getSenderId, peerId)
                                        .eq(FamilyMessage::getReceiverId, userId)))
                        .orderByAsc(FamilyMessage::getCreatedAt)
                        .orderByAsc(FamilyMessage::getId)
        ).getRecords();

        return toVoList(records);
    }

    public void sendFamilyGroup(Long senderId, MessageSendRequest req) {
        User sender = userMapper.selectById(senderId);
        if (sender == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        Long elderId;
        if ("elder".equals(sender.getRole())) {
            elderId = senderId;
        } else if ("child".equals(sender.getRole())) {
            elderId = req.getElderId() != null ? req.getElderId() : req.getReceiverId();
            if (elderId == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST);
            }
            assertCanMessagePeer(senderId, elderId);
        } else {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        List<BindingRelation> activeRelations = activeFamilyRelations(elderId);
        if (activeRelations.isEmpty() && elderId.equals(senderId)) {
            throw new BusinessException(4006, "暂未绑定家人，无法发送");
        }

        if (!StringUtils.hasText(req.getMsgType())) {
            req.setMsgType("text");
        }

        broadcastToFamily(elderId, senderId, req);
    }

=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
    public List<FamilyMessageVO> received(Long userId, int page, int size) {
        List<FamilyMessage> records = familyMessageMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<FamilyMessage>()
                        .eq(FamilyMessage::getReceiverId, userId)
                        .orderByDesc(FamilyMessage::getCreatedAt)
        ).getRecords();

<<<<<<< HEAD
        return toVoList(records);
    }

    public void markRead(Long id, Long userId) {
        FamilyMessage msg = familyMessageMapper.selectById(id);
        if (msg != null && userId.equals(msg.getReceiverId())) {
            msg.setIsRead(1);
            familyMessageMapper.updateById(msg);
        }
    }

    public void recall(Long messageId, Long userId) {
        FamilyMessage msg = familyMessageMapper.selectById(messageId);
        if (msg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!userId.equals(msg.getSenderId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (msg.getStatus() != null && msg.getStatus() == 1) {
            return;
        }
        if (msg.getCreatedAt() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        if (Duration.between(msg.getCreatedAt(), LocalDateTime.now()).toMinutes() >= 2) {
            throw new BusinessException(ResultCode.MESSAGE_RECALL_TIMEOUT);
        }
        msg.setStatus(1);
        msg.setRecalledAt(LocalDateTime.now());
        familyMessageMapper.updateById(msg);
    }

    private List<FamilyMessageVO> getFamilyGroupMessages(Long elderId, int page, int size, boolean ascending) {
        List<Long> familyIds = resolveFamilyIds(elderId);
        if (familyIds.size() <= 1) {
            return List.of();
        }

        int querySize = Math.max(size * Math.max(2, familyIds.size() - 1), size);
        List<FamilyMessage> records = familyMessageMapper.selectPage(
                new Page<>(page, querySize),
                new LambdaQueryWrapper<FamilyMessage>()
                        .in(FamilyMessage::getSenderId, familyIds)
                        .in(FamilyMessage::getReceiverId, familyIds)
                        .orderByDesc(FamilyMessage::getCreatedAt)
                        .orderByDesc(FamilyMessage::getId)
        ).getRecords();

        List<FamilyMessage> visibleRecords = collapseFamilyBroadcastDuplicates(records, size);
        if (ascending) {
            Collections.reverse(visibleRecords);
        }
        return toVoList(visibleRecords);
    }

    private void broadcastToFamily(Long elderId, Long senderId, MessageSendRequest req) {
        List<Long> recipients = new ArrayList<>();
        if (!elderId.equals(senderId)) {
            recipients.add(elderId);
        }
        for (BindingRelation relation : activeFamilyRelations(elderId)) {
            Long childId = relation.getChildId();
            if (childId != null && !childId.equals(senderId)) {
                recipients.add(childId);
            }
        }
        if (recipients.isEmpty()) {
            throw new BusinessException(4006, "暂未绑定家人，无法发送");
        }

        for (Long receiverId : recipients) {
            FamilyMessage msg = FamilyMessage.builder()
                    .senderId(senderId)
                    .receiverId(receiverId)
                    .msgType(req.getMsgType())
                    .content(req.getContent())
                    .mediaUrl(req.getMediaUrl())
                    .duration(req.getDuration())
                    .isRead(0)
                    .build();
            familyMessageMapper.insert(msg);
            pushService.pushToUser(
                    receiverId,
                    "小伴消息",
                    "家庭聊天群有一条新消息",
                    Map.of("type", "family_msg", "elderId", String.valueOf(elderId)));
        }
    }

    private Long resolveElderIdForFamilyGroup(Long userId, Long elderId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if ("elder".equals(user.getRole())) {
            return userId;
        }
        if ("child".equals(user.getRole())) {
            if (elderId == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST);
            }
            assertCanMessagePeer(userId, elderId);
            return elderId;
        }
        throw new BusinessException(ResultCode.FORBIDDEN);
    }

    private List<Long> resolveFamilyIds(Long elderId) {
        List<Long> familyIds = new ArrayList<>();
        familyIds.add(elderId);
        activeFamilyRelations(elderId).stream()
                .map(BindingRelation::getChildId)
                .filter(Objects::nonNull)
                .forEach(familyIds::add);
        return familyIds;
    }

    private FamilyMessage findLatestFamilyMessage(List<Long> familyIds) {
        if (familyIds.size() <= 1) {
            return null;
        }
        int querySize = Math.max(20, familyIds.size() * 5);
        List<FamilyMessage> records = familyMessageMapper.selectPage(
                new Page<>(1, querySize),
                new LambdaQueryWrapper<FamilyMessage>()
                        .in(FamilyMessage::getSenderId, familyIds)
                        .in(FamilyMessage::getReceiverId, familyIds)
                        .orderByDesc(FamilyMessage::getCreatedAt)
                        .orderByDesc(FamilyMessage::getId)
        ).getRecords();
        List<FamilyMessage> collapsed = collapseFamilyBroadcastDuplicates(records, 1);
        return collapsed.isEmpty() ? null : collapsed.get(0);
    }

    private int countFamilyUnread(Long childId, List<Long> familyIds) {
        Long count = familyMessageMapper.selectCount(
                new LambdaQueryWrapper<FamilyMessage>()
                        .eq(FamilyMessage::getReceiverId, childId)
                        .eq(FamilyMessage::getIsRead, 0)
                        .in(FamilyMessage::getSenderId, familyIds)
                        .ne(FamilyMessage::getSenderId, childId));
        return count != null ? count.intValue() : 0;
    }

    private String formatGroupPreview(FamilyMessage latest, List<Long> familyIds) {
        if (latest == null) {
            return "暂无消息";
        }
        if (latest.getStatus() != null && latest.getStatus() == 1) {
            return "[已撤回]";
        }
        String content = resolveLastContentPreview(latest);
        User sender = userMapper.selectById(latest.getSenderId());
        if (sender != null && familyIds.size() > 2) {
            return resolveDisplayName(sender) + ": " + content;
        }
        return content;
    }

    private String resolveLastContentPreview(FamilyMessage latest) {
        if (latest == null) {
            return "暂无消息";
        }
        if (latest.getStatus() != null && latest.getStatus() == 1) {
            return "[已撤回]";
        }
        if ("photo".equals(latest.getMsgType())) {
            return "[图片]";
        }
        if ("voice".equals(latest.getMsgType())) {
            return "[语音]";
        }
        return latest.getContent() != null ? latest.getContent() : "暂无消息";
    }

    private void assertCanMessagePeer(Long userId, Long peerId) {
        if (userId == null || peerId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        if (userId.equals(peerId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        User user = userMapper.selectById(userId);
        User peer = userMapper.selectById(peerId);
        if (user == null || peer == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if ("child".equals(user.getRole()) && "elder".equals(peer.getRole())) {
            if (!hasActiveBinding(peerId, userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            return;
        }
        if ("elder".equals(user.getRole()) && "child".equals(peer.getRole())) {
            if (!hasActiveBinding(userId, peerId)) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            return;
        }
        throw new BusinessException(ResultCode.FORBIDDEN);
    }

    private boolean hasActiveBinding(Long elderId, Long childId) {
        Long count = bindingRelationMapper.selectCount(
                new LambdaQueryWrapper<BindingRelation>()
                        .eq(BindingRelation::getElderId, elderId)
                        .eq(BindingRelation::getChildId, childId)
                        .eq(BindingRelation::getStatus, "active"));
        return count != null && count > 0;
    }

    private List<FamilyMessageVO> toVoList(List<FamilyMessage> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
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

<<<<<<< HEAD
    private List<BindingRelation> activeFamilyRelations(Long elderId) {
        return bindingRelationMapper.selectList(
                new LambdaQueryWrapper<BindingRelation>()
                        .eq(BindingRelation::getElderId, elderId)
                        .eq(BindingRelation::getStatus, "active")
        );
    }

    private List<FamilyMessage> collapseFamilyBroadcastDuplicates(List<FamilyMessage> records, int maxSize) {
        List<FamilyMessage> result = new ArrayList<>();
        Set<String> seenBroadcasts = new HashSet<>();
        for (FamilyMessage message : records) {
            String key = message.getSenderId() + "|"
                    + Objects.toString(message.getMsgType(), "") + "|"
                    + Objects.toString(message.getContent(), "") + "|"
                    + Objects.toString(message.getMediaUrl(), "") + "|"
                    + Objects.toString(message.getDuration(), "") + "|"
                    + Objects.toString(message.getCreatedAt(), "");
            if (!seenBroadcasts.add(key)) {
                continue;
            }
            result.add(message);
            if (result.size() >= maxSize) {
                break;
            }
        }
        return result;
=======
    public void markRead(Long id, Long userId) {
        FamilyMessage msg = familyMessageMapper.selectById(id);
        if (msg != null && userId.equals(msg.getReceiverId())) {
            msg.setIsRead(1);
            familyMessageMapper.updateById(msg);
        }
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
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
