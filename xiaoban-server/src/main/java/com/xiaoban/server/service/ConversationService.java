package com.xiaoban.server.service;

import com.xiaoban.server.dto.ChatRequest;
import com.xiaoban.server.vo.ChatResponse;
import com.xiaoban.server.vo.ConversationVO;

import java.time.LocalDate;
import java.util.List;

public interface ConversationService {

    ChatResponse chat(Long elderId, ChatRequest request);

    List<ConversationVO> getPendingConversations(Long childId);

    List<ConversationVO> getHistory(Long elderId, LocalDate date);

    void confirm(Long conversationId, Long childId);

    void correct(Long conversationId, Long childId, String correction);
}
