package com.xiaoban.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("daily_report")
public class DailyReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long elderId;

    private LocalDate reportDate;

    private Integer totalConversations;

    private String summaryText;

    private String healthFlags;

    private Integer moodScore;

    private String topicDistribution;

    private LocalDateTime createdAt;
}
