package com.fazhitong.consultation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.consultation.entity.LawyerClient;
import com.fazhitong.consultation.mapper.LawyerClientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LawyerClientService {

    private final LawyerClientMapper clientMapper;

    public LawyerClient create(LawyerClient c) {
        if (c.getClientType() == null || c.getClientType().isBlank()) {
            c.setClientType("PERSONAL");
        }
        clientMapper.insert(c);
        return c;
    }

    public PageResult<LawyerClient> list(Long lawyerId, String keyword, PageParam pageParam) {
        LambdaQueryWrapper<LawyerClient> wrapper = new LambdaQueryWrapper<>();
        if (lawyerId != null) wrapper.eq(LawyerClient::getLawyerId, lawyerId);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(LawyerClient::getClientName, keyword)
                    .or().like(LawyerClient::getPhone, keyword));
        }
        wrapper.orderByDesc(LawyerClient::getCreateTime);
        Page<LawyerClient> page = clientMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public LawyerClient update(LawyerClient c) {
        LawyerClient existing = clientMapper.selectById(c.getId());
        if (existing == null) {
            throw new BusinessException("客户不存在");
        }
        clientMapper.updateById(c);
        return clientMapper.selectById(c.getId());
    }

    public void delete(Long id) {
        clientMapper.deleteById(id);
    }
}
