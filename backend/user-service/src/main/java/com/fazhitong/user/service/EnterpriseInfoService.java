package com.fazhitong.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.user.entity.EnterpriseInfo;
import com.fazhitong.user.mapper.EnterpriseInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnterpriseInfoService {

    private final EnterpriseInfoMapper enterpriseInfoMapper;

    public EnterpriseInfo getByUserId(Long userId) {
        EnterpriseInfo info = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, userId));
        if (info == null) {
            throw new BusinessException("企业信息不存在");
        }
        return info;
    }

    public EnterpriseInfo submit(EnterpriseInfo info) {
        EnterpriseInfo existing = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, info.getUserId()));
        if (existing != null) {
            info.setId(existing.getId());
            info.setStatus(0);
            enterpriseInfoMapper.updateById(info);
        } else {
            info.setStatus(0);
            enterpriseInfoMapper.insert(info);
        }
        return info;
    }

    public EnterpriseInfo audit(Long id, int status) {
        EnterpriseInfo info = enterpriseInfoMapper.selectById(id);
        if (info == null) {
            throw new BusinessException("企业信息不存在");
        }
        info.setStatus(status);
        enterpriseInfoMapper.updateById(info);
        return info;
    }

    public PageResult<EnterpriseInfo> list(Integer status, PageParam pageParam) {
        LambdaQueryWrapper<EnterpriseInfo> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(EnterpriseInfo::getStatus, status);
        }
        wrapper.orderByDesc(EnterpriseInfo::getCreateTime);
        Page<EnterpriseInfo> page = enterpriseInfoMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public Map<String, Object> count() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", enterpriseInfoMapper.selectCount(null));
        result.put("certified", enterpriseInfoMapper.selectCount(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getStatus, 1)));
        result.put("pending", enterpriseInfoMapper.selectCount(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getStatus, 0)));
        return result;
    }
}
