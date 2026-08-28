package com.fazhitong.user.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.user.entity.Notification;
import com.fazhitong.user.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/user/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 分页查询通知列表
     */
    @GetMapping("/list")
    public ApiResult<PageResult<Notification>> list(
            @RequestParam Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer isRead,
            PageParam pageParam) {
        return ApiResult.success(notificationService.list(userId, type, isRead, pageParam));
    }

    /**
     * 查询未读通知数量
     */
    @GetMapping("/unread-count")
    public ApiResult<Long> unreadCount(@RequestParam Long userId) {
        return ApiResult.success(notificationService.unreadCount(userId));
    }

    /**
     * 标记单条通知为已读
     */
    @PostMapping("/read/{id}")
    public ApiResult<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ApiResult.success();
    }

    /**
     * 标记用户所有通知为已读
     */
    @PostMapping("/read-all")
    public ApiResult<Void> markAllAsRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return ApiResult.success();
    }

    /**
     * 创建通知（内部调用）
     */
    @PostMapping
    public ApiResult<Notification> create(@RequestBody Notification notification) {
        return ApiResult.success(notificationService.create(notification));
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ApiResult.success();
    }
}
