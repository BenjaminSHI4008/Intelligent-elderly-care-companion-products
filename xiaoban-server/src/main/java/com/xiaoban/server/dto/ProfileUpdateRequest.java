package com.xiaoban.server.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号必须为11位数字，且以1开头、第二位为3-9")
    private String phone;

    private String nickname;

    @Pattern(regexp = "^$|^(男|女)$", message = "性别只能选择男或女")
    private String gender;

    @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "生日格式应为yyyy-MM-dd")
    private String birthday;

    @Pattern(regexp = "^$|^\\d{3,20}$", message = "紧急联系人电话格式不正确")
    private String emergencyContact;
}
