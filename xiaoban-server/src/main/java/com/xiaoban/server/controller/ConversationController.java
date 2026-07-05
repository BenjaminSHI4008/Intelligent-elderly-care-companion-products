package com.xiaoban.server.controller;

import com.xiaoban.server.common.Result;
import com.xiaoban.server.dto.ChatRequest;
import com.xiaoban.server.service.ConversationService;
import com.xiaoban.server.vo.ChatResponse;
import com.xiaoban.server.vo.ConversationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "对话模块", description = "AI语音对话及对话管理")
@RestController
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @Operation(summary = "老人端发送对话")
    @PostMapping("/api/voice/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request, Authentication auth) {
        Long elderId = (Long) auth.getPrincipal();
        return Result.success(conversationService.chat(elderId, request));
    }

    @Operation(summary = "子女端查看待确认对话")
    @GetMapping("/api/conversations/pending")
    public Result<List<ConversationVO>> pending(Authentication auth) {
        Long childId = (Long) auth.getPrincipal();
        return Result.success(conversationService.getPendingConversations(childId));
    }

    @Operation(summary = "查看历史对话")
    @GetMapping("/api/conversations/history")
    public Result<List<ConversationVO>> history(
            @RequestParam Long elderId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(conversationService.getHistory(elderId, date));
    }

    @Operation(summary = "子女确认对话")
    @PostMapping("/api/conversations/confirm")
    public Result<Void> confirm(@RequestBody Map<String, Long> body, Authentication auth) {
        Long childId = (Long) auth.getPrincipal();
        conversationService.confirm(body.get("conversationId"), childId);
        return Result.success();
    }

    @Operation(summary = "子女纠正对话")
    @PostMapping("/api/conversations/correct")
    public Result<Void> correct(@RequestBody Map<String, Object> body, Authentication auth) {
        Long childId = (Long) auth.getPrincipal();
        Long conversationId = Long.valueOf(body.get("conversationId").toString());
        String correction = (String) body.get("correction");
        conversationService.correct(conversationId, childId, correction);
        return Result.success();
    }
}
