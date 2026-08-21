package com.fabaotong.contract.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.common.exception.BusinessException;
import com.fabaotong.contract.entity.ContractRecord;
import com.fabaotong.contract.entity.EnterpriseContract;
import com.fabaotong.contract.mapper.ContractRecordMapper;
import com.fabaotong.contract.mapper.EnterpriseContractMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRecordMapper recordMapper;
    private final EnterpriseContractMapper enterpriseContractMapper;

    public ContractRecord uploadReview(Long userId, Long enterpriseId, String title, String fileUrl) {
        ContractRecord record = new ContractRecord();
        record.setUserId(userId);
        record.setEnterpriseId(enterpriseId);
        record.setTitle(title);
        record.setFileUrl(fileUrl);
        record.setStatus(0);
        recordMapper.insert(record);
        return record;
    }

    public ContractRecord reviewResult(Long id, String riskReport, String riskLevel) {
        ContractRecord record = recordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("合同审查记录不存在");
        }
        record.setRiskReport(riskReport);
        record.setRiskLevel(riskLevel);
        record.setStatus(1);
        record.setReviewTime(LocalDateTime.now());
        recordMapper.updateById(record);
        return record;
    }

    public PageResult<ContractRecord> listRecords(Long userId, Long enterpriseId, PageParam pageParam) {
        LambdaQueryWrapper<ContractRecord> wrapper = new LambdaQueryWrapper<ContractRecord>()
                .orderByDesc(ContractRecord::getCreateTime);
        if (userId != null) wrapper.eq(ContractRecord::getUserId, userId);
        if (enterpriseId != null) wrapper.eq(ContractRecord::getEnterpriseId, enterpriseId);
        Page<ContractRecord> page = recordMapper.selectPage(new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public EnterpriseContract createEnterpriseContract(EnterpriseContract contract) {
        enterpriseContractMapper.insert(contract);
        return contract;
    }

    public PageResult<EnterpriseContract> listEnterpriseContracts(Long enterpriseId, PageParam pageParam) {
        Page<EnterpriseContract> page = enterpriseContractMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<EnterpriseContract>()
                        .eq(EnterpriseContract::getEnterpriseId, enterpriseId)
                        .orderByDesc(EnterpriseContract::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }
}
