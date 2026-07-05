package com.xiaoban.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("family_message")
public class FamilyMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long senderId;

    private Long receiverId;

    private String msgType;

    private String content;

    private String mediaUrl;

    private Integer duration;

    private Integer isRead;

<<<<<<< HEAD
    /** 0=正常 1=已撤回 */
    private Integer status;

    private LocalDateTime recalledAt;

=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
    private LocalDateTime createdAt;
}
