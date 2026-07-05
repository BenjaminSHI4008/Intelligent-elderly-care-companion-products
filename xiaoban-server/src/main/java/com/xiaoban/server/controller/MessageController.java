package com.xiaoban.server.controller;

import com.xiaoban.server.common.Result;
import com.xiaoban.server.dto.ConversationSummaryVO;
import com.xiaoban.server.dto.FamilyGroupMemberVO;
import com.xiaoban.server.dto.FamilyMessageVO;
import com.xiaoban.server.dto.MessageSendRequest;
import com.xiaoban.server.entity.FamilyMessage;
import com.xiaoban.server.service.FamilyMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "家庭消息模块")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final FamilyMessageService familyMessageService;

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public Result<FamilyMessage> send(@Valid @RequestBody MessageSendRequest req, Authentication auth) {
        Long senderId = (Long) auth.getPrincipal();
        return Result.success(familyMessageService.send(senderId, req));
    }

    @Operation(summary = "子女端会话列表摘要")
    @GetMapping("/conversations")
    public Result<List<ConversationSummaryVO>> conversations(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(familyMessageService.listConversations(userId));
    }

    @Operation(summary = "获取收到的消息")
    @GetMapping("/received")
    public Result<List<FamilyMessageVO>> received(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(familyMessageService.received(userId, page, size));
    }

    @Operation(summary = "获取家庭聊天群消息（老人端或子女端）")
    @GetMapping("/family-group")
    public Result<List<FamilyMessageVO>> familyGroup(
            @RequestParam(required = false) Long elderId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "false") boolean ascending,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(familyMessageService.familyGroup(userId, elderId, page, size, ascending));
    }

    @Operation(summary = "获取家庭聊天群成员")
    @GetMapping("/family-group/members")
    public Result<List<FamilyGroupMemberVO>> familyGroupMembers(
            @RequestParam(required = false) Long elderId,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(familyMessageService.familyGroupMembers(userId, elderId));
    }

    @Operation(summary = "获取与指定家人的聊天记录（子女端兼容：返回家庭群聊）")
    @GetMapping("/conversation")
    public Result<List<FamilyMessageVO>> conversation(
            @RequestParam Long peerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(familyMessageService.conversation(userId, peerId, page, size));
    }

    @Operation(summary = "发送家庭聊天群消息（老人/子女）")
    @PostMapping("/family-group/send")
    public Result<Void> sendFamilyGroup(@RequestBody MessageSendRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        familyMessageService.sendFamilyGroup(userId, req);
        return Result.success();
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        familyMessageService.markRead(id, userId);
        return Result.success();
    }

    @Operation(summary = "撤回消息（2分钟内）")
    @PostMapping("/{id}/recall")
    public Result<Void> recall(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        familyMessageService.recall(id, userId);
        return Result.success();
    }
}
