package com.xiaoban.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyGroupMemberVO {

    private Long userId;
    private String nickname;
    private String role;
    private boolean elder;
}
