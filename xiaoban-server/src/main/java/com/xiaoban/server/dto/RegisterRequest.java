package com.xiaoban.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号必须为11位数字，且以1开头、第二位为3-9")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(elder|child)$", message = "角色只能是elder或child")
    private String role;

    private String nickname;
}
