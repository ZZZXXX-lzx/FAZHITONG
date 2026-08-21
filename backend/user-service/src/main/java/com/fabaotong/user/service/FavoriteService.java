package com.fabaotong.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.common.exception.BusinessException;
import com.fabaotong.user.entity.Favorite;
import com.fabaotong.user.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 收藏服务
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;

    /**
     * 收藏（唯一约束防重复）
     *
     * @param favorite 收藏对象
     * @return 创建后的收藏
     */
    public Favorite create(Favorite favorite) {
        if (favorite == null) {
            throw new BusinessException("收藏内容不能为空");
        }
        if (favorite.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (favorite.getTargetType() == null || favorite.getTargetType().isEmpty()) {
            throw new BusinessException("收藏对象类型不能为空");
        }
        if (favorite.getTargetId() == null) {
            throw new BusinessException("收藏对象ID不能为空");
        }
        // 唯一约束防重复
        long existCount = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, favorite.getUserId())
                        .eq(Favorite::getTargetType, favorite.getTargetType())
                        .eq(Favorite::getTargetId, favorite.getTargetId()));
        if (existCount > 0) {
            throw new BusinessException("已收藏过该对象，不能重复收藏");
        }
        favoriteMapper.insert(favorite);
        return favorite;
    }

    /**
     * 取消收藏
     *
     * @param userId     用户ID
     * @param targetType 收藏对象类型
     * @param targetId   收藏对象ID
     */
    public void remove(Long userId, String targetType, Long targetId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (targetType == null || targetType.isEmpty()) {
            throw new BusinessException("收藏对象类型不能为空");
        }
        if (targetId == null) {
            throw new BusinessException("收藏对象ID不能为空");
        }
        int deleted = favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getTargetType, targetType)
                        .eq(Favorite::getTargetId, targetId));
        if (deleted == 0) {
            throw new BusinessException("收藏记录不存在");
        }
    }

    /**
     * 查询我的收藏（分页）
     *
     * @param userId     用户ID
     * @param targetType 收藏对象类型（可选）
     * @return 分页结果
     */
    public PageResult<Favorite> list(Long userId, String targetType, PageParam pageParam) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq(Favorite::getTargetType, targetType);
        }
        wrapper.orderByDesc(Favorite::getCreateTime);
        Page<Favorite> page = favoriteMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 检查是否已收藏
     *
     * @param userId     用户ID
     * @param targetType 收藏对象类型
     * @param targetId   收藏对象ID
     * @return true-已收藏，false-未收藏
     */
    public boolean check(Long userId, String targetType, Long targetId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (targetType == null || targetType.isEmpty()) {
            throw new BusinessException("收藏对象类型不能为空");
        }
        if (targetId == null) {
            throw new BusinessException("收藏对象ID不能为空");
        }
        long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getTargetType, targetType)
                        .eq(Favorite::getTargetId, targetId));
        return count > 0;
    }

    /**
     * 收藏数量
     *
     * @param userId     用户ID
     * @param targetType 收藏对象类型（可选）
     * @return 收藏数量
     */
    public long count(Long userId, String targetType) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq(Favorite::getTargetType, targetType);
        }
        return favoriteMapper.selectCount(wrapper);
    }
}
