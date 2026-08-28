package com.fazhitong.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fazhitong.user.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
