package com.xiaoban.server.common;

public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或令牌已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    PHONE_ALREADY_EXISTS(4001, "该手机号已注册"),
    LOGIN_FAILED(4002, "手机号或密码错误"),
    BIND_CODE_INVALID(4003, "绑定码无效或已过期"),
    BIND_CODE_USED(4004, "绑定码已被使用"),
    ALREADY_BOUND(4005, "已存在绑定关系"),
    SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
