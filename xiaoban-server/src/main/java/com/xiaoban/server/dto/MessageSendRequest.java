package com.xiaoban.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageSendRequest {

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    private String msgType;
    private String content;
    private String mediaUrl;
    private Integer duration;
}
