package com.xiaoban.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.common.BusinessException;
import com.xiaoban.server.common.ResultCode;
import com.xiaoban.server.dto.ChatRequest;
import com.xiaoban.server.entity.BindingRelation;
import com.xiaoban.server.entity.Conversation;
import com.xiaoban.server.mapper.BindingRelationMapper;
import com.xiaoban.server.mapper.ConversationMapper;
import com.xiaoban.server.service.*;
import com.xiaoban.server.vo.ChatResponse;
import com.xiaoban.server.vo.ConversationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationMapper conversationMapper;
    private final BindingRelationMapper bindingRelationMapper;
    private final KeywordDetectionService keywordDetectionService;
    private final PrivacyDetectionService privacyDetectionService;
    private final AiChatService aiChatService;
    private final PushService pushService;

    @Override
    public ChatResponse chat(Long elderId, ChatRequest request) {
        String category = keywordDetectionService.detectCategory(request.getText());
        boolean isPrivate = privacyDetectionService.isPrivate(request.getText());
        String answer = aiChatService.chat(request.getText(), category);

        Conversation conversation = Conversation.builder()
                .elderId(elderId)
                .sessionId(request.getSessionId())
                .userQuestion(request.getText())
                .aiAnswer(answer)
                .category(category)
                .isPrivate(isPrivate ? 1 : 0)
                .confirmStatus("health".equals(category) && !isPrivate ? "pending" : "none")
                .build();
        conversationMapper.insert(conversation);

        if (!isPrivate) {
            if ("health".equals(category)) {
                pushService.pushToElderFamily(elderId, "健康关注", "妈妈询问了用药相关问题，请查看确认");
            } else if ("urgent".equals(category)) {
                pushService.pushToElderFamily(elderId, "⚠️ 紧急情况", "妈妈可能遇到紧急情况，请立即查看");
            }
        }

        return ChatResponse.builder()
                .answer(answer)
                .category(category)
                .safetyTip("health".equals(category) ? "最好问问医生哦" : null)
                .conversationId(conversation.getId())
                .build();
    }

    @Override
    public List<ConversationVO> getPendingConversations(Long childId) {
        List<Long> elderIds = bindingRelationMapper.selectList(
                new LambdaQueryWrapper<BindingRelation>()
                        .eq(BindingRelation::getChildId, childId)
                        .eq(BindingRelation::getStatus, "active")
        ).stream().map(BindingRelation::getElderId).collect(Collectors.toList());

        if (elderIds.isEmpty()) return List.of();

        return conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .in(Conversation::getElderId, elderIds)
                        .eq(Conversation::getConfirmStatus, "pending")
                        .orderByDesc(Conversation::getCreatedAt)
        ).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<ConversationVO> getHistory(Long elderId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getElderId, elderId)
                        .eq(Conversation::getIsPrivate, 0)
                        .between(Conversation::getCreatedAt, start, end)
                        .orderByAsc(Conversation::getCreatedAt)
        ).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void confirm(Long conversationId, Long childId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        conversation.setConfirmStatus("confirmed");
        conversation.setCorrectedBy(childId);
        conversation.setCorrectedAt(LocalDateTime.now());
        conversationMapper.updateById(conversation);
    }

    @Override
    public void correct(Long conversationId, Long childId, String correction) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        conversation.setConfirmStatus("corrected");
        conversation.setChildCorrection(correction);
        conversation.setCorrectedBy(childId);
        conversation.setCorrectedAt(LocalDateTime.now());
        conversationMapper.updateById(conversation);

        pushService.pushToUser(conversation.getElderId(), "小伴提醒", "您女儿说，" + correction, null);
    }

    private ConversationVO toVO(Conversation c) {
        return ConversationVO.builder()
                .id(c.getId())
                .elderId(c.getElderId())
                .sessionId(c.getSessionId())
                .userQuestion(c.getUserQuestion())
                .aiAnswer(c.getAiAnswer())
                .category(c.getCategory())
                .confirmStatus(c.getConfirmStatus())
                .childCorrection(c.getChildCorrection())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
