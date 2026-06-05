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
import com.xiaoban.server.vo.BindResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BindService {

    private final BindCodeTempMapper bindCodeTempMapper;
    private final BindingRelationMapper bindingRelationMapper;
    private final UserMapper userMapper;

    public String generateCode(Long elderId) {
        // 过期旧码
        bindCodeTempMapper.delete(
                new LambdaQueryWrapper<BindCodeTemp>()
                        .eq(BindCodeTemp::getElderId, elderId)
                        .eq(BindCodeTemp::getIsUsed, 0));

        String code = RandomUtil.randomNumbers(6);
        BindCodeTemp temp = BindCodeTemp.builder()
                .elderId(elderId)
                .code(code)
                .expireAt(LocalDateTime.now().plusMinutes(5))
                .isUsed(0)
                .build();
        bindCodeTempMapper.insert(temp);
        return code;
    }

    public BindResultVO verifyCode(Long childId, String code) {
        BindCodeTemp temp = bindCodeTempMapper.selectOne(
                new LambdaQueryWrapper<BindCodeTemp>()
                        .eq(BindCodeTemp::getCode, code)
                        .eq(BindCodeTemp::getIsUsed, 0)
                        .gt(BindCodeTemp::getExpireAt, LocalDateTime.now()));

        if (temp == null) throw new BusinessException(ResultCode.BIND_CODE_INVALID);

        createBinding(temp.getElderId(), childId, "code", code);

        temp.setIsUsed(1);
        bindCodeTempMapper.updateById(temp);

        User elder = userMapper.selectById(temp.getElderId());
        return BindResultVO.builder()
                .elderId(temp.getElderId())
                .elderNickname(elder != null ? elder.getNickname() : "")
                .bindTime(LocalDateTime.now())
                .build();
    }

    public void bindBluetooth(Long childId, Long elderUserId) {
        createBinding(elderUserId, childId, "bluetooth", null);
    }

    public List<BindingRelation> getRelations(Long userId, String role) {
        LambdaQueryWrapper<BindingRelation> query = new LambdaQueryWrapper<>();
        if ("elder".equals(role)) {
            query.eq(BindingRelation::getElderId, userId);
        } else {
            query.eq(BindingRelation::getChildId, userId);
        }
        return bindingRelationMapper.selectList(query);
    }

    public void unbind(Long relationId) {
        BindingRelation relation = bindingRelationMapper.selectById(relationId);
        if (relation == null) throw new BusinessException(ResultCode.NOT_FOUND);
        relation.setStatus("unbound");
        bindingRelationMapper.updateById(relation);
    }

    private void createBinding(Long elderId, Long childId, String bindType, String code) {
        Long exists = bindingRelationMapper.selectCount(
                new LambdaQueryWrapper<BindingRelation>()
                        .eq(BindingRelation::getElderId, elderId)
                        .eq(BindingRelation::getChildId, childId)
                        .eq(BindingRelation::getStatus, "active"));
        if (exists > 0) throw new BusinessException(ResultCode.ALREADY_BOUND);

        BindingRelation relation = BindingRelation.builder()
                .elderId(elderId)
                .childId(childId)
                .bindType(bindType)
                .bindcode(code)
                .status("active")
                .build();
        bindingRelationMapper.insert(relation);
    }
}
