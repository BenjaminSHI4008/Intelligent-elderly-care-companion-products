package com.xiaoban.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageSendRequest {

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /** 子女端发家庭群聊时指定老人ID */
    private Long elderId;

    private String msgType;
    private String content;
    private String mediaUrl;
    private Integer duration;
}
