package com.xiaoban.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryVO {

    private Long peerId;
    private String peerName;
    private String lastMsgType;
    private String lastContent;
    private LocalDateTime lastTime;
    private int unreadCount;
    private boolean muted;
}
