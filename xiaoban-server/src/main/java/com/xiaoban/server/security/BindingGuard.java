package com.xiaoban.server.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.common.BusinessException;
import com.xiaoban.server.common.ResultCode;
import com.xiaoban.server.entity.BindingRelation;
import com.xiaoban.server.mapper.BindingRelationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Centralized binding-based access control for child-side reads/writes.
 *
 * <p>All child-facing endpoints that expose an elder's data (dashboard, report,
 * reminder, conversation history) must go through this guard so that a child can
 * only ever see elders they currently have an {@code active} binding with. This
 * mirrors the peer-messaging permission model already enforced in
 * {@code FamilyMessageService} and keeps the rule in one place (SRP + DIP).
 */
@Component
@RequiredArgsConstructor
public class BindingGuard {

    private static final String STATUS_ACTIVE = "active";

    private final BindingRelationMapper bindingRelationMapper;

    /**
     * Asserts the given child has an active binding to the given elder.
     *
     * @throws BusinessException {@code BAD_REQUEST} when ids are missing,
     *                           {@code FORBIDDEN} when no active binding exists.
     */
    public void assertChildBoundToElder(Long childId, Long elderId) {
        if (childId == null || elderId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        if (!hasActiveBinding(childId, elderId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    public boolean hasActiveBinding(Long childId, Long elderId) {
        Long count = bindingRelationMapper.selectCount(
                new LambdaQueryWrapper<BindingRelation>()
                        .eq(BindingRelation::getElderId, elderId)
                        .eq(BindingRelation::getChildId, childId)
                        .eq(BindingRelation::getStatus, STATUS_ACTIVE));
        return count != null && count > 0;
    }
}
