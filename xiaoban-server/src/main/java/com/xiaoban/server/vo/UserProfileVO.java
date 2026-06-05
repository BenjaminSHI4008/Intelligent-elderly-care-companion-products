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
public class UserProfileVO {

    private Long userId;
    private String phone;
    private String role;
    private String nickname;
    private String avatarUrl;
    private String deviceModel;
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;
}
