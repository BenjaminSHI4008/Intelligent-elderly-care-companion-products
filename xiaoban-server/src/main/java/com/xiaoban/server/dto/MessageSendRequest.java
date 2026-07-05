package com.xiaoban.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageSendRequest {

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

<<<<<<< HEAD
    /** 子女端发家庭群聊时指定老人ID */
    private Long elderId;

=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
    private String msgType;
    private String content;
    private String mediaUrl;
    private Integer duration;
}
