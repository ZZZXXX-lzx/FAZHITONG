package com.fazhitong.contract.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.contract.entity.InvestmentRecord;
import com.fazhitong.contract.mapper.InvestmentRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestmentRecordService {

    private final InvestmentRecordMapper investmentRecordMapper;

    public InvestmentRecord create(InvestmentRecord record) {
        if (record.getStatus() == null || record.getStatus().isBlank()) {
            record.setStatus("PLANNED");
        }
        if (record.getType() == null || record.getType().isBlank()) {
            record.setType("FINANCING");
        }
        investmentRecordMapper.insert(record);
        return record;
    }

    public PageResult<InvestmentRecord> list(Long enterpriseId, String type, String status, PageParam pageParam) {
        LambdaQueryWrapper<InvestmentRecord> wrapper = new LambdaQueryWrapper<>();
        if (enterpriseId != null) wrapper.eq(InvestmentRecord::getEnterpriseId, enterpriseId);
        if (type != null && !type.isBlank()) wrapper.eq(InvestmentRecord::getType, type);
        if (status != null && !status.isBlank()) wrapper.eq(InvestmentRecord::getStatus, status);
        wrapper.orderByDesc(InvestmentRecord::getInvestDate);
        Page<InvestmentRecord> page = investmentRecordMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public InvestmentRecord update(InvestmentRecord record) {
        InvestmentRecord existing = investmentRecordMapper.selectById(record.getId());
        if (existing == null) {
            throw new BusinessException("投融资记录不存在");
        }
        investmentRecordMapper.updateById(record);
        return record;
    }

    public void delete(Long id) {
        investmentRecordMapper.deleteById(id);
    }
}
