package com.xiaoban.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.common.BusinessException;
import com.xiaoban.server.common.ResultCode;
import com.xiaoban.server.dto.LoginRequest;
import com.xiaoban.server.dto.ProfileUpdateRequest;
import com.xiaoban.server.dto.RegisterRequest;
import com.xiaoban.server.entity.User;
import com.xiaoban.server.mapper.UserMapper;
import com.xiaoban.server.security.JwtUtil;
import com.xiaoban.server.service.AuthService;
import com.xiaoban.server.vo.LoginVO;
import com.xiaoban.server.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_EXISTS);
        }

        User user = User.builder()
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .nickname(request.getNickname() != null ? request.getNickname() : "")
                .build();
        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getUserId(), user.getRole());

        return LoginVO.builder()
                .token(token)
                .userId(user.getUserId())
                .role(user.getRole())
                .nickname(user.getNickname())
                .build();
    }

    @Override
    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone())
        );
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getRole());

        return LoginVO.builder()
                .token(token)
                .userId(user.getUserId())
                .role(user.getRole())
                .nickname(user.getNickname())
                .build();
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        return toProfileVO(user);
    }

    @Override
    public UserProfileVO updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getPhone, request.getPhone())
                            .ne(User::getUserId, userId)
            );
            if (count > 0) {
                throw new BusinessException(ResultCode.PHONE_ALREADY_EXISTS);
            }
            user.setPhone(request.getPhone());
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (request.getEmergencyContact() != null) {
            user.setEmergencyContact(request.getEmergencyContact());
        }

        userMapper.updateById(user);
        return toProfileVO(user);
    }

    private UserProfileVO toProfileVO(User user) {
        return UserProfileVO.builder()
                .userId(user.getUserId())
                .phone(user.getPhone())
                .role(user.getRole())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .emergencyContact(user.getEmergencyContact())
                .avatarUrl(user.getAvatarUrl())
                .deviceModel(user.getDeviceModel())
                .lastActiveAt(user.getLastActiveAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
