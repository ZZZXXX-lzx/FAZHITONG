package com.fazhitong.contract.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.contract.entity.LegalReview;
import com.fazhitong.contract.mapper.LegalReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LegalReviewService {

    private final LegalReviewMapper legalReviewMapper;

    public LegalReview create(LegalReview record) {
        if (record.getStatus() == null || record.getStatus().isBlank()) {
            record.setStatus("PENDING");
        }
        if (record.getReviewType() == null || record.getReviewType().isBlank()) {
            record.setReviewType("CONTRACT");
        }
        legalReviewMapper.insert(record);
        return record;
    }

    public PageResult<LegalReview> list(Long enterpriseId, String reviewType, String status, PageParam pageParam) {
        LambdaQueryWrapper<LegalReview> wrapper = new LambdaQueryWrapper<>();
        if (enterpriseId != null) wrapper.eq(LegalReview::getEnterpriseId, enterpriseId);
        if (reviewType != null && !reviewType.isBlank()) wrapper.eq(LegalReview::getReviewType, reviewType);
        if (status != null && !status.isBlank()) wrapper.eq(LegalReview::getStatus, status);
        wrapper.orderByDesc(LegalReview::getCreateTime);
        Page<LegalReview> page = legalReviewMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public LegalReview update(LegalReview record) {
        LegalReview existing = legalReviewMapper.selectById(record.getId());
        if (existing == null) {
            throw new BusinessException("审核记录不存在");
        }
        legalReviewMapper.updateById(record);
        return record;
    }

    public void delete(Long id) {
        legalReviewMapper.deleteById(id);
    }

    /** 审核：设置审核状态、审核人、意见与审核时间 */
    public LegalReview review(Long id, LegalReview req) {
        LegalReview existing = legalReviewMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("审核记录不存在");
        }
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            existing.setStatus(req.getStatus());
        }
        if (req.getReviewer() != null) {
            existing.setReviewer(req.getReviewer());
        }
        if (req.getOpinion() != null) {
            existing.setOpinion(req.getOpinion());
        }
        existing.setReviewTime(LocalDateTime.now());
        legalReviewMapper.updateById(existing);
        return existing;
    }
}
