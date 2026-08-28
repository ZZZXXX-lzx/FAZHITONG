package com.fabaotong.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.common.exception.BusinessException;
import com.fabaotong.user.entity.Feedback;
import com.fabaotong.user.mapper.FeedbackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 意见反馈服务
 */
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;

    /**
     * 提交反馈
     *
     * @param feedback 反馈对象
     * @return 创建后的反馈
     */
    public Feedback create(Feedback feedback) {
        if (feedback == null) {
            throw new BusinessException("反馈内容不能为空");
        }
        if (feedback.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (feedback.getTitle() == null || feedback.getTitle().isEmpty()) {
            throw new BusinessException("反馈标题不能为空");
        }
        if (feedback.getContent() == null || feedback.getContent().isEmpty()) {
            throw new BusinessException("反馈内容不能为空");
        }
        if (feedback.getType() == null || feedback.getType().isEmpty()) {
            feedback.setType("SUGGESTION");
        }
        if (feedback.getStatus() == null) {
            feedback.setStatus(0);
        }
        feedbackMapper.insert(feedback);
        return feedback;
    }

    /**
     * 查询我的反馈（分页）
     *
     * @param userId 用户ID
     * @return 分页结果
     */
    public PageResult<Feedback> myFeedbacks(Long userId, PageParam pageParam) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getUserId, userId)
                .orderByDesc(Feedback::getCreateTime);
        Page<Feedback> page = feedbackMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 管理员查看所有反馈（分页，可按状态筛选）
     *
     * @param status 状态（可选）
     * @return 分页结果
     */
    public PageResult<Feedback> list(Integer status, PageParam pageParam) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Feedback::getStatus, status);
        }
        wrapper.orderByDesc(Feedback::getCreateTime);
        Page<Feedback> page = feedbackMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 管理员回复反馈（同时设置status=1, replyTime=now）
     *
     * @param id     反馈ID
     * @param reply  回复内容
     * @return 更新后的反馈
     */
    public Feedback reply(Long id, String reply) {
        if (id == null) {
            throw new BusinessException("反馈ID不能为空");
        }
        if (reply == null || reply.isEmpty()) {
            throw new BusinessException("回复内容不能为空");
        }
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException("反馈不存在");
        }
        feedback.setReply(reply);
        feedback.setStatus(1);
        feedback.setReplyTime(LocalDateTime.now());
        feedbackMapper.updateById(feedback);
        return feedback;
    }
}
