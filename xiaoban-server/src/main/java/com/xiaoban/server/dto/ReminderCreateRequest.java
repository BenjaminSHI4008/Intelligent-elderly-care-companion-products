package com.xiaoban.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReminderCreateRequest {

    @NotNull(message = "老人ID不能为空")
    private Long elderId;

    @NotBlank(message = "提醒内容不能为空")
    private String content;

    @NotBlank(message = "提醒时间不能为空")
    private String remindTime;

    private String repeatType = "daily";
}
