package com.fazhitong.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.user.entity.Review;
import com.fazhitong.user.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价评分服务
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;

    /**
     * 提交评价
     * 防止重复评价：同一用户对同一target只能评价一次
     *
     * @param review 评价对象
     * @return 创建后的评价
     */
    public Review create(Review review) {
        if (review == null) {
            throw new BusinessException("评价内容不能为空");
        }
        if (review.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (review.getTargetType() == null || review.getTargetType().isEmpty()) {
            throw new BusinessException("评价对象类型不能为空");
        }
        if (review.getTargetId() == null) {
            throw new BusinessException("评价对象ID不能为空");
        }
        // 防止重复评价
        long existCount = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getUserId, review.getUserId())
                        .eq(Review::getTargetType, review.getTargetType())
                        .eq(Review::getTargetId, review.getTargetId()));
        if (existCount > 0) {
            throw new BusinessException("您已评价过该对象，不能重复评价");
        }
        if (review.getRating() == null) {
            review.setRating(5);
        }
        if (review.getStatus() == null) {
            review.setStatus(1);
        }
        reviewMapper.insert(review);
        return review;
    }

    /**
     * 查询某对象的评价列表（分页，只查status=1的）
     *
     * @param targetType 评价对象类型
     * @param targetId  评价对象ID
     * @return 分页结果
     */
    public PageResult<Review> list(String targetType, Long targetId, PageParam pageParam) {
        if (targetType == null || targetType.isEmpty()) {
            throw new BusinessException("评价对象类型不能为空");
        }
        if (targetId == null) {
            throw new BusinessException("评价对象ID不能为空");
        }
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getTargetType, targetType)
                .eq(Review::getTargetId, targetId)
                .eq(Review::getStatus, 1)
                .orderByDesc(Review::getCreateTime);
        Page<Review> page = reviewMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 查询我的评价（分页）
     *
     * @param userId 用户ID
     * @return 分页结果
     */
    public PageResult<Review> myReviews(Long userId, PageParam pageParam) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreateTime);
        Page<Review> page = reviewMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 评价汇总（平均分+总数）
     *
     * @param targetType 评价对象类型
     * @param targetId   评价对象ID
     * @return 包含avgRating和total的Map
     */
    public Map<String, Object> summary(String targetType, Long targetId) {
        if (targetType == null || targetType.isEmpty()) {
            throw new BusinessException("评价对象类型不能为空");
        }
        if (targetId == null) {
            throw new BusinessException("评价对象ID不能为空");
        }
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getTargetType, targetType)
                        .eq(Review::getTargetId, targetId)
                        .eq(Review::getStatus, 1));
        Map<String, Object> result = new HashMap<>();
        long total = reviews.size();
        double avgRating = 0.0;
        if (total > 0) {
            int sum = 0;
            for (Review review : reviews) {
                if (review.getRating() != null) {
                    sum += review.getRating();
                }
            }
            avgRating = Math.round((double) sum / total * 10) / 10.0;
        }
        result.put("avgRating", avgRating);
        result.put("total", total);
        return result;
    }

    /**
     * 管理员操作评价状态（隐藏/显示）
     *
     * @param id     评价ID
     * @param status 状态：1-显示，0-隐藏
     * @return 更新后的评价
     */
    public Review updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException("评价ID不能为空");
        }
        if (status == null) {
            throw new BusinessException("状态不能为空");
        }
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        review.setStatus(status);
        reviewMapper.updateById(review);
        return review;
    }
}
