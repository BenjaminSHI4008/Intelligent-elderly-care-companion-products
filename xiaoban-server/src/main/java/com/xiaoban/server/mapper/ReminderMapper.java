package com.xiaoban.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoban.server.entity.Reminder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReminderMapper extends BaseMapper<Reminder> {
}
