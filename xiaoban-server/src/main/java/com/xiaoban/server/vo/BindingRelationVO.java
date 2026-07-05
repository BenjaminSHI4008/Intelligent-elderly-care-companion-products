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
public class BindingRelationVO {

    private Long id;
    private Long elderId;
    private Long childId;
    private String elderNickname;
    private String childNickname;
    private String elderPhoneMasked;
    private String childPhoneMasked;
    private String bindType;
    private String bindTypeLabel;
    private String status;
    private LocalDateTime bindTime;
}
