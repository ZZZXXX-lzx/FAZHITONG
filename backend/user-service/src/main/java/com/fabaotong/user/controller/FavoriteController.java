package com.fabaotong.user.controller;

import com.fabaotong.common.dto.ApiResult;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.user.entity.Favorite;
import com.fabaotong.user.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/user/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 收藏（唯一约束防重复）
     */
    @PostMapping
    public ApiResult<Favorite> create(@RequestBody Favorite favorite) {
        return ApiResult.success(favoriteService.create(favorite));
    }

    /**
     * 取消收藏
     */
    @DeleteMapping
    public ApiResult<Void> remove(
            @RequestParam Long userId,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        favoriteService.remove(userId, targetType, targetId);
        return ApiResult.success();
    }

    /**
     * 查询我的收藏（分页）
     */
    @GetMapping("/list")
    public ApiResult<PageResult<Favorite>> list(
            @RequestParam Long userId,
            @RequestParam(required = false) String targetType,
            PageParam pageParam) {
        return ApiResult.success(favoriteService.list(userId, targetType, pageParam));
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public ApiResult<Boolean> check(
            @RequestParam Long userId,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return ApiResult.success(favoriteService.check(userId, targetType, targetId));
    }

    /**
     * 收藏数量
     */
    @GetMapping("/count")
    public ApiResult<Long> count(
            @RequestParam Long userId,
            @RequestParam(required = false) String targetType) {
        return ApiResult.success(favoriteService.count(userId, targetType));
    }
}
