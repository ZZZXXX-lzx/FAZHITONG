package com.fazhitong.consultation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.consultation.entity.LawyerCase;
import com.fazhitong.consultation.mapper.LawyerCaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LawyerCaseService {

    private final LawyerCaseMapper caseMapper;

    public LawyerCase create(LawyerCase c) {
        if (c.getStatus() == null || c.getStatus().isBlank()) {
            c.setStatus("IN_PROGRESS");
        }
        caseMapper.insert(c);
        return c;
    }

    public PageResult<LawyerCase> list(Long lawyerId, String status, String keyword, PageParam pageParam) {
        LambdaQueryWrapper<LawyerCase> wrapper = new LambdaQueryWrapper<>();
        if (lawyerId != null) wrapper.eq(LawyerCase::getLawyerId, lawyerId);
        if (status != null && !status.isBlank()) wrapper.eq(LawyerCase::getStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(LawyerCase::getCaseName, keyword)
                    .or().like(LawyerCase::getClientName, keyword)
                    .or().like(LawyerCase::getCaseType, keyword));
        }
        wrapper.orderByDesc(LawyerCase::getCreateTime);
        Page<LawyerCase> page = caseMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public LawyerCase update(LawyerCase c) {
        LawyerCase existing = caseMapper.selectById(c.getId());
        if (existing == null) {
            throw new BusinessException("案件不存在");
        }
        caseMapper.updateById(c);
        return caseMapper.selectById(c.getId());
    }

    /** 状态流转：结案 / 归档 / 重新承办 */
    public LawyerCase transition(Long id, String action) {
        LawyerCase c = caseMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("案件不存在");
        }
        switch (action) {
            case "CLOSE" -> {
                c.setStatus("CLOSED");
                c.setCloseTime(LocalDateTime.now());
            }
            case "ARCHIVE" -> c.setStatus("ARCHIVED");
            case "REOPEN" -> {
                c.setStatus("IN_PROGRESS");
                c.setCloseTime(null);
            }
            default -> throw new BusinessException("非法的状态操作");
        }
        caseMapper.updateById(c);
        return c;
    }

    public void delete(Long id) {
        caseMapper.deleteById(id);
    }

    /** 案件统计：按状态分组计数 */
    public Map<String, Object> stats(Long lawyerId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", caseMapper.selectCount(
                new LambdaQueryWrapper<LawyerCase>().eq(LawyerCase::getLawyerId, lawyerId)));
        result.put("inProgress", caseMapper.selectCount(
                new LambdaQueryWrapper<LawyerCase>()
                        .eq(LawyerCase::getLawyerId, lawyerId)
                        .eq(LawyerCase::getStatus, "IN_PROGRESS")));
        result.put("closed", caseMapper.selectCount(
                new LambdaQueryWrapper<LawyerCase>()
                        .eq(LawyerCase::getLawyerId, lawyerId)
                        .eq(LawyerCase::getStatus, "CLOSED")));
        return result;
    }
}
