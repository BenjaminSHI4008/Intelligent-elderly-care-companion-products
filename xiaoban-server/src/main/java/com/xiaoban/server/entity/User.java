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
@TableName("user")
public class User {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    private String phone;

    private String passwordHash;

    private String role;

    private String nickname;

    private String gender;

    private String birthday;

    private String avatarUrl;

    private String deviceModel;

    private LocalDateTime lastActiveAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
