package com.xiaoban.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "文本不能为空")
    private String text;

    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
}
