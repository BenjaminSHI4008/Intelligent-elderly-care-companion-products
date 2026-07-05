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
    private Integer status;
    private boolean recalled;
    private LocalDateTime recalledAt;
    private LocalDateTime createdAt;
    private String senderName;

    public static FamilyMessageVO from(FamilyMessage message, String senderName) {
        boolean recalled = message.getStatus() != null && message.getStatus() == 1;
        return FamilyMessageVO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .msgType(message.getMsgType())
                .content(recalled ? null : message.getContent())
                .mediaUrl(recalled ? null : message.getMediaUrl())
                .duration(message.getDuration())
                .isRead(message.getIsRead())
                .status(message.getStatus() != null ? message.getStatus() : 0)
                .recalled(recalled)
                .recalledAt(message.getRecalledAt())
                .createdAt(message.getCreatedAt())
                .senderName(senderName != null && !senderName.isBlank() ? senderName : "家人")
                .build();
    }
}
