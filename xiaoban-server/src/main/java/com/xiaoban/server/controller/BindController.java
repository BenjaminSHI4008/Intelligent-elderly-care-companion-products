package com.xiaoban.server.controller;

import com.xiaoban.server.common.Result;
import com.xiaoban.server.dto.VerifyCodeRequest;
import com.xiaoban.server.entity.User;
import com.xiaoban.server.mapper.UserMapper;
import com.xiaoban.server.service.BindService;
import com.xiaoban.server.vo.BindResultVO;
import com.xiaoban.server.vo.BindingRelationVO;
import com.xiaoban.server.vo.GenerateCodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "设备绑定模块")
@RestController
@RequestMapping("/api/bind")
@RequiredArgsConstructor
public class BindController {

    private final BindService bindService;
    private final UserMapper userMapper;

    @Operation(summary = "生成绑定码（老人端调用）")
    @PostMapping("/generate-code")
    public Result<GenerateCodeVO> generateCode(Authentication auth) {
        Long elderId = (Long) auth.getPrincipal();
        return Result.success(bindService.generateCode(elderId));
    }

    @Operation(summary = "使用绑定码绑定（子女端调用）")
    @PostMapping("/verify-code")
    public Result<BindResultVO> verifyCode(@RequestBody VerifyCodeRequest req, Authentication auth) {
        Long childId = (Long) auth.getPrincipal();
        return Result.success(bindService.verifyCode(childId, req.getCode()));
    }

    @Operation(summary = "蓝牙绑定（子女端调用）")
    @PostMapping("/bluetooth")
    public Result<Void> bluetooth(@RequestBody Map<String, Long> body, Authentication auth) {
        Long childId = (Long) auth.getPrincipal();
        Long elderUserId = body.get("elderUserId");
        if (elderUserId == null) {
            elderUserId = body.get("elderId");
        }
        bindService.bindBluetooth(childId, elderUserId);
        return Result.success();
    }

    @Operation(summary = "查看绑定关系列表（含昵称）")
    @GetMapping("/relations")
    public Result<List<BindingRelationVO>> relations(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userMapper.selectById(userId);
        return Result.success(bindService.getRelationDetails(userId, user.getRole()));
    }

    @Operation(summary = "解除绑定")
    @DeleteMapping("/{relationId}")
    public Result<Void> unbind(@PathVariable Long relationId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        bindService.unbind(relationId, userId);
        return Result.success();
    }
}
