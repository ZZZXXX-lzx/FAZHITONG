package com.fazhitong.consultation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.consultation.entity.LegalAid;
import com.fazhitong.consultation.mapper.LegalAidMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LegalAidService {

    private final LegalAidMapper legalAidMapper;

    /**
     * 提交申请(默认status=0)
     */
    public LegalAid apply(LegalAid legalAid) {
        legalAid.setStatus(0);
        legalAidMapper.insert(legalAid);
        return legalAid;
    }

    /**
     * 我的申请列表(分页, orderByDesc createTime)
     */
    public PageResult<LegalAid> my(Long userId, PageParam pageParam) {
        Page<LegalAid> page = legalAidMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<LegalAid>()
                        .eq(LegalAid::getUserId, userId)
                        .orderByDesc(LegalAid::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 管理员查看所有(分页, status可选, orderByDesc createTime)
     */
    public PageResult<LegalAid> list(Integer status, PageParam pageParam) {
        LambdaQueryWrapper<LegalAid> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(LegalAid::getStatus, status);
        }
        wrapper.orderByDesc(LegalAid::getCreateTime);
        Page<LegalAid> page = legalAidMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 详情(null检查)
     */
    public LegalAid getById(Long id) {
        LegalAid legalAid = legalAidMapper.selectById(id);
        if (legalAid == null) {
            throw new BusinessException("法律援助申请不存在");
        }
        return legalAid;
    }

    /**
     * 审核(设置status, reviewRemark, assignedLawyerId可选, reviewTime=now)
     */
    public LegalAid audit(Long id, Integer status, String remark, Long assignedLawyerId) {
        LegalAid legalAid = legalAidMapper.selectById(id);
        if (legalAid == null) {
            throw new BusinessException("法律援助申请不存在");
        }
        legalAid.setStatus(status);
        legalAid.setReviewRemark(remark);
        if (assignedLawyerId != null) {
            legalAid.setAssignedLawyerId(assignedLawyerId);
        }
        legalAid.setReviewTime(LocalDateTime.now());
        legalAidMapper.updateById(legalAid);
        return legalAid;
    }
}
