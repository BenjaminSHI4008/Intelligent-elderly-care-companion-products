package com.xiaoban.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.common.BusinessException;
import com.xiaoban.server.common.ResultCode;
import com.xiaoban.server.entity.BindingRelation;
import com.xiaoban.server.entity.User;
import com.xiaoban.server.mapper.BindingRelationMapper;
import com.xiaoban.server.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FamilyAccessService {

    private final UserMapper userMapper;
    private final BindingRelationMapper bindingRelationMapper;

    public User requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    public User requireChild(Long childId) {
        User child = requireUser(childId);
        if (!"child".equals(child.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return child;
    }

    public User requireElder(Long elderId) {
        User elder = requireUser(elderId);
        if (!"elder".equals(elder.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return elder;
    }

    public void assertCanAccessElder(Long userId, Long elderId) {
        if (elderId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        User user = requireUser(userId);
        if ("elder".equals(user.getRole())) {
            if (!Objects.equals(userId, elderId)) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            return;
        }
        if ("child".equals(user.getRole())) {
            assertChildCanAccessElder(userId, elderId);
            return;
        }
        throw new BusinessException(ResultCode.FORBIDDEN);
    }

    public void assertChildCanAccessElder(Long childId, Long elderId) {
        requireChild(childId);
        requireElder(elderId);
        if (!hasActiveBinding(elderId, childId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    public boolean hasActiveBinding(Long elderId, Long childId) {
        if (elderId == null || childId == null) {
            return false;
        }
        Long count = bindingRelationMapper.selectCount(
                new LambdaQueryWrapper<BindingRelation>()
                        .eq(BindingRelation::getElderId, elderId)
                        .eq(BindingRelation::getChildId, childId)
                        .eq(BindingRelation::getStatus, "active"));
        return count != null && count > 0;
    }

    public List<BindingRelation> activeRelationsForElder(Long elderId) {
        requireElder(elderId);
        return bindingRelationMapper.selectList(
                new LambdaQueryWrapper<BindingRelation>()
                        .eq(BindingRelation::getElderId, elderId)
                        .eq(BindingRelation::getStatus, "active"));
    }

    public List<Long> activeFamilyIds(Long elderId) {
        List<Long> familyIds = new ArrayList<>();
        familyIds.add(elderId);
        for (BindingRelation relation : activeRelationsForElder(elderId)) {
            if (relation.getChildId() != null) {
                familyIds.add(relation.getChildId());
            }
        }
        return familyIds;
    }
}
