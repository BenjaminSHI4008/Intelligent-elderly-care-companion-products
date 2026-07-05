package com.xiaoban.server.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO {

    private Long id;
    private Long elderId;
    private String sessionId;
    private String userQuestion;
    private String aiAnswer;
    private String category;
    private String confirmStatus;
    private String childCorrection;
    private LocalDateTime createdAt;
}
