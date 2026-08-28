package com.fabaotong.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.common.exception.BusinessException;
import com.fabaotong.user.entity.Notification;
import com.fabaotong.user.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 消息通知服务
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    /**
     * 分页查询通知列表
     * 按isRead倒序，再按createTime倒序
     *
     * @param userId 用户ID
     * @param type   通知类型（可选）
     * @param isRead 是否已读（可选）
     * @return 分页结果
     */
    public PageResult<Notification> list(Long userId, String type, Integer isRead, PageParam pageParam) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        // 先按isRead倒序，再按createTime倒序
        wrapper.orderByDesc(Notification::getIsRead);
        wrapper.orderByDesc(Notification::getCreateTime);
        Page<Notification> page = notificationMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 查询未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    public long unreadCount(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
    }

    /**
     * 标记单条通知为已读
     *
     * @param id 通知ID
     */
    public void markAsRead(Long id) {
        if (id == null) {
            throw new BusinessException("通知ID不能为空");
        }
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        notification.setIsRead(1);
        notificationMapper.updateById(notification);
    }

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     */
    public void markAllAsRead(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
                        .set(Notification::getIsRead, 1));
    }

    /**
     * 创建通知（内部调用）
     *
     * @param notification 通知对象
     * @return 创建后的通知
     */
    public Notification create(Notification notification) {
        if (notification == null) {
            throw new BusinessException("通知内容不能为空");
        }
        if (notification.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (notification.getTitle() == null || notification.getTitle().isEmpty()) {
            throw new BusinessException("通知标题不能为空");
        }
        if (notification.getType() == null || notification.getType().isEmpty()) {
            notification.setType("SYSTEM");
        }
        if (notification.getIsRead() == null) {
            notification.setIsRead(0);
        }
        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * 删除通知
     *
     * @param id 通知ID
     */
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException("通知ID不能为空");
        }
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        notificationMapper.deleteById(id);
    }
}
