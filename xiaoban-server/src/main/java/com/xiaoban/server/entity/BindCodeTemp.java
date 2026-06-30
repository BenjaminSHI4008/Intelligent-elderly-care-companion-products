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
@TableName("bind_code_temp")
public class BindCodeTemp {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long elderId;

    private String code;

    private LocalDateTime expireAt;

    private Integer isUsed;
}
