package com.xiaoban.server.controller;

import com.xiaoban.server.common.Result;
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

    @Operation(summary = "获取收到的消息")
    @GetMapping("/received")
    public Result<List<FamilyMessageVO>> received(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(familyMessageService.received(userId, page, size));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        familyMessageService.markRead(id, userId);
        return Result.success();
    }
}
