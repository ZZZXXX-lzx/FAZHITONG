package com.fazhitong.contract.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.contract.entity.IpRecord;
import com.fazhitong.contract.mapper.IpRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IpRecordService {

    private final IpRecordMapper ipRecordMapper;

    public IpRecord create(IpRecord record) {
        if (record.getStatus() == null || record.getStatus().isBlank()) {
            record.setStatus("PENDING");
        }
        ipRecordMapper.insert(record);
        return record;
    }

    public PageResult<IpRecord> list(Long enterpriseId, String ipType, String status, PageParam pageParam) {
        LambdaQueryWrapper<IpRecord> wrapper = new LambdaQueryWrapper<>();
        if (enterpriseId != null) wrapper.eq(IpRecord::getEnterpriseId, enterpriseId);
        if (ipType != null && !ipType.isBlank()) wrapper.eq(IpRecord::getIpType, ipType);
        if (status != null && !status.isBlank()) wrapper.eq(IpRecord::getStatus, status);
        wrapper.orderByDesc(IpRecord::getCreateTime);
        Page<IpRecord> page = ipRecordMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        for (IpRecord r : page.getRecords()) {
            r.setDaysToExpire(daysToExpire(r.getExpireDate()));
        }
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public IpRecord update(IpRecord record) {
        IpRecord existing = ipRecordMapper.selectById(record.getId());
        if (existing == null) {
            throw new BusinessException("知识产权记录不存在");
        }
        ipRecordMapper.updateById(record);
        return record;
    }

    public void delete(Long id) {
        ipRecordMapper.deleteById(id);
    }

    /** 即将到期的知识产权（默认 90 天内） */
    public List<IpRecord> listExpiring(Long enterpriseId, int days) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(days);
        List<IpRecord> list = ipRecordMapper.selectList(
                new LambdaQueryWrapper<IpRecord>()
                        .eq(IpRecord::getEnterpriseId, enterpriseId)
                        .eq(IpRecord::getStatus, "GRANTED")
                        .isNotNull(IpRecord::getExpireDate)
                        .le(IpRecord::getExpireDate, threshold)
                        .orderByAsc(IpRecord::getExpireDate));
        for (IpRecord r : list) {
            r.setDaysToExpire(daysToExpire(r.getExpireDate()));
        }
        return list;
    }

    private Integer daysToExpire(LocalDateTime expireDate) {
        if (expireDate == null) return null;
        return (int) java.time.Duration.between(LocalDateTime.now(), expireDate).toDays();
    }
}
