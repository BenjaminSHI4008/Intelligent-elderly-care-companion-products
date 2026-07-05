package com.xiaoban.server.dto;

import com.xiaoban.server.entity.FamilyMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMessageVO {

    private Long id;
    private Long senderId;
    private Long receiverId;
    private String msgType;
    private String content;
    private String mediaUrl;
    private Integer duration;
    private Integer isRead;
<<<<<<< HEAD
    private Integer status;
    private boolean recalled;
    private LocalDateTime recalledAt;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
    private LocalDateTime createdAt;
    private String senderName;

    public static FamilyMessageVO from(FamilyMessage message, String senderName) {
<<<<<<< HEAD
        boolean recalled = message.getStatus() != null && message.getStatus() == 1;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
        return FamilyMessageVO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .msgType(message.getMsgType())
<<<<<<< HEAD
                .content(recalled ? null : message.getContent())
                .mediaUrl(recalled ? null : message.getMediaUrl())
                .duration(message.getDuration())
                .isRead(message.getIsRead())
                .status(message.getStatus() != null ? message.getStatus() : 0)
                .recalled(recalled)
                .recalledAt(message.getRecalledAt())
=======
                .content(message.getContent())
                .mediaUrl(message.getMediaUrl())
                .duration(message.getDuration())
                .isRead(message.getIsRead())
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
                .createdAt(message.getCreatedAt())
                .senderName(senderName != null && !senderName.isBlank() ? senderName : "家人")
                .build();
    }
}
