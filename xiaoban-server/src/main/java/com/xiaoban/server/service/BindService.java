package com.xiaoban.server.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.common.BusinessException;
import com.xiaoban.server.common.ResultCode;
import com.xiaoban.server.entity.BindCodeTemp;
import com.xiaoban.server.entity.BindingRelation;
import com.xiaoban.server.entity.User;
import com.xiaoban.server.mapper.BindCodeTempMapper;
import com.xiaoban.server.mapper.BindingRelationMapper;
import com.xiaoban.server.mapper.UserMapper;
import com.xiaoban.server.util.PhoneMaskUtil;
import com.xiaoban.server.vo.BindResultVO;
import com.xiaoban.server.vo.BindingRelationVO;
import com.xiaoban.server.vo.GenerateCodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BindService {

    private static final int CODE_VALID_MINUTES = 5;

    private final BindCodeTempMapper bindCodeTempMapper;
    private final BindingRelationMapper bindingRelationMapper;
    private final UserMapper userMapper;
    private final PushService pushService;

    public GenerateCodeVO generateCode(Long elderId) {
        User user = userMapper.selectById(elderId);
        if (user == null || !"elder".equals(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        bindCodeTempMapper.delete(
                new LambdaQueryWrapper<BindCodeTemp>()
                        .eq(BindCodeTemp::getElderId, elderId)
                        .eq(BindCodeTemp::getIsUsed, 0));

        String code = RandomUtil.randomNumbers(6);
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(CODE_VALID_MINUTES);
        BindCodeTemp temp = BindCodeTemp.builder()
                .elderId(elderId)
                .code(code)
                .expireAt(expireAt)
                .isUsed(0)
                .build();
        bindCodeTempMapper.insert(temp);

        return GenerateCodeVO.builder()
                .code(code)
                .expireAt(expireAt)
                .expiresInSeconds(CODE_VALID_MINUTES * 60)
                .build();
    }

    public BindResultVO verifyCode(Long childId, String code) {
        User child = userMapper.selectById(childId);
        if (child == null || !"child".equals(child.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        BindCodeTemp temp = bindCodeTempMapper.selectOne(
                new LambdaQueryWrapper<BindCodeTemp>()
                        .eq(BindCodeTemp::getCode, code)
                        .eq(BindCodeTemp::getIsUsed, 0)
                        .gt(BindCodeTemp::getExpireAt, LocalDateTime.now()));

        if (temp == null) {
            throw new BusinessException(ResultCode.BIND_CODE_INVALID);
        }

        createBinding(temp.getElderId(), childId, "code", code);

        temp.setIsUsed(1);
        bindCodeTempMapper.updateById(temp);

        User elder = userMapper.selectById(temp.getElderId());
        String childName = resolveDisplayName(child);
        pushService.pushToUser(temp.getElderId(), "绑定成功",
                childName + " 已成功与您绑定",
                java.util.Map.of("type", "bind_success"));

        return BindResultVO.builder()
                .elderId(temp.getElderId())
                .elderNickname(elder != null ? elder.getNickname() : "")
                .bindTime(LocalDateTime.now())
                .build();
    }

    public void bindBluetooth(Long childId, Long elderUserId) {
        User child = userMapper.selectById(childId);
        if (child == null || !"child".equals(child.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (elderUserId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        User elder = userMapper.selectById(elderUserId);
        if (elder == null || !"elder".equals(elder.getRole())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        createBinding(elderUserId, childId, "bluetooth", null);

        String childName = resolveDisplayName(child);
        pushService.pushToUser(elderUserId, "绑定成功",
                childName + " 已成功与您绑定",
                java.util.Map.of("type", "bind_success"));
    }

    public List<BindingRelationVO> getRelationDetails(Long userId, String role) {
        return getRelations(userId, role).stream()
                .map(this::toRelationVO)
                .collect(Collectors.toList());
    }

    public List<BindingRelation> getRelations(Long userId, String role) {
        if (userId == null || role == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        LambdaQueryWrapper<BindingRelation> query = new LambdaQueryWrapper<>();
        if ("elder".equals(role)) {
            query.eq(BindingRelation::getElderId, userId);
        } else if ("child".equals(role)) {
            query.eq(BindingRelation::getChildId, userId);
        } else {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        query.orderByDesc(BindingRelation::getBindTime);
        return bindingRelationMapper.selectList(query);
    }

    public void unbind(Long relationId, Long userId) {
        BindingRelation relation = bindingRelationMapper.selectById(relationId);
        if (relation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!relation.getElderId().equals(userId) && !relation.getChildId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        relation.setStatus("unbound");
        bindingRelationMapper.updateById(relation);
    }

    private void createBinding(Long elderId, Long childId, String bindType, String code) {
        Long exists = bindingRelationMapper.selectCount(
                new LambdaQueryWrapper<BindingRelation>()
                        .eq(BindingRelation::getElderId, elderId)
                        .eq(BindingRelation::getChildId, childId)
                        .eq(BindingRelation::getStatus, "active"));
        if (exists > 0) {
            throw new BusinessException(ResultCode.ALREADY_BOUND);
        }

        BindingRelation relation = BindingRelation.builder()
                .elderId(elderId)
                .childId(childId)
                .bindType(bindType)
                .bindcode(code)
                .status("active")
                .build();
        bindingRelationMapper.insert(relation);
    }

    private BindingRelationVO toRelationVO(BindingRelation relation) {
        User elder = userMapper.selectById(relation.getElderId());
        User child = userMapper.selectById(relation.getChildId());

        return BindingRelationVO.builder()
                .id(relation.getId())
                .elderId(relation.getElderId())
                .childId(relation.getChildId())
                .elderNickname(elder != null ? elder.getNickname() : "")
                .childNickname(child != null ? child.getNickname() : "")
                .elderPhoneMasked(elder != null ? PhoneMaskUtil.mask(elder.getPhone()) : "")
                .childPhoneMasked(child != null ? PhoneMaskUtil.mask(child.getPhone()) : "")
                .bindType(relation.getBindType())
                .bindTypeLabel(resolveBindTypeLabel(relation.getBindType()))
                .status(relation.getStatus())
                .bindTime(relation.getBindTime())
                .build();
    }

    private String resolveBindTypeLabel(String bindType) {
        if ("bluetooth".equals(bindType)) {
            return "蓝牙绑定";
        }
        return "设备码绑定";
    }

    private String resolveDisplayName(User user) {
        if (user == null) {
            return "您的家人";
        }
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return PhoneMaskUtil.mask(user.getPhone());
    }
}
