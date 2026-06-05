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
@TableName("conversation")
public class Conversation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long elderId;

    private String sessionId;

    private String userQuestion;

    private String aiAnswer;

    private String category;

    private Integer isPrivate;

    private String confirmStatus;

    private String childCorrection;

    private Long correctedBy;

    private LocalDateTime correctedAt;

    private LocalDateTime createdAt;
}
