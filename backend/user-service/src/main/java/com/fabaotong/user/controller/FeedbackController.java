package com.fabaotong.user.controller;

import com.fabaotong.common.dto.ApiResult;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.user.entity.Feedback;
import com.fabaotong.user.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 意见反馈控制器
 */
@RestController
@RequestMapping("/user/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 提交反馈
     */
    @PostMapping
    public ApiResult<Feedback> create(@RequestBody Feedback feedback) {
        return ApiResult.success(feedbackService.create(feedback));
    }

    /**
     * 我的反馈（分页）
     */
    @GetMapping("/my")
    public ApiResult<PageResult<Feedback>> my(
            @RequestParam Long userId,
            PageParam pageParam) {
        return ApiResult.success(feedbackService.myFeedbacks(userId, pageParam));
    }

    /**
     * 管理员查看所有反馈（分页，可按状态筛选）
     */
    @GetMapping("/list")
    public ApiResult<PageResult<Feedback>> list(
            @RequestParam(required = false) Integer status,
            PageParam pageParam) {
        return ApiResult.success(feedbackService.list(status, pageParam));
    }

    /**
     * 管理员回复反馈（同时设置status=1, replyTime=now）
     */
    @PostMapping("/{id}/reply")
    public ApiResult<Feedback> reply(
            @PathVariable Long id,
            @RequestParam String reply) {
        return ApiResult.success(feedbackService.reply(id, reply));
    }
}
