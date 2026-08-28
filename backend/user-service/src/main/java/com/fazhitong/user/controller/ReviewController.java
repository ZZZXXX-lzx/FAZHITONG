package com.fazhitong.user.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.user.entity.Review;
import com.fazhitong.user.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评价评分控制器
 */
@RestController
@RequestMapping("/user/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 提交评价
     */
    @PostMapping
    public ApiResult<Review> create(@RequestBody Review review) {
        return ApiResult.success(reviewService.create(review));
    }

    /**
     * 查询某对象的评价列表（分页，status=1）
     */
    @GetMapping("/list")
    public ApiResult<PageResult<Review>> list(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            PageParam pageParam) {
        return ApiResult.success(reviewService.list(targetType, targetId, pageParam));
    }

    /**
     * 查询我的评价（分页）
     */
    @GetMapping("/my")
    public ApiResult<PageResult<Review>> my(
            @RequestParam Long userId,
            PageParam pageParam) {
        return ApiResult.success(reviewService.myReviews(userId, pageParam));
    }

    /**
     * 评价汇总（平均分+总数）
     */
    @GetMapping("/summary")
    public ApiResult<Map<String, Object>> summary(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return ApiResult.success(reviewService.summary(targetType, targetId));
    }

    /**
     * 管理员操作评价状态（隐藏/显示）
     */
    @PostMapping("/{id}/status")
    public ApiResult<Review> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return ApiResult.success(reviewService.updateStatus(id, status));
    }
}
