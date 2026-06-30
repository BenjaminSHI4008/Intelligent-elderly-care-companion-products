package com.xiaoban.server.service;

import com.xiaoban.server.dto.LoginRequest;
import com.xiaoban.server.dto.ProfileUpdateRequest;
import com.xiaoban.server.dto.RegisterRequest;
import com.xiaoban.server.vo.LoginVO;
import com.xiaoban.server.vo.UserProfileVO;

public interface AuthService {

    LoginVO register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    UserProfileVO getProfile(Long userId);

    UserProfileVO updateProfile(Long userId, ProfileUpdateRequest request);
}
