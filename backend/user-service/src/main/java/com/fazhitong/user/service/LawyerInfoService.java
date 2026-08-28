package com.fazhitong.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.user.entity.LawyerInfo;
import com.fazhitong.user.mapper.LawyerInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LawyerInfoService {

    private final LawyerInfoMapper lawyerInfoMapper;

    public LawyerInfo getByUserId(Long userId) {
        LawyerInfo info = lawyerInfoMapper.selectOne(
                new LambdaQueryWrapper<LawyerInfo>().eq(LawyerInfo::getUserId, userId));
        if (info == null) {
            throw new BusinessException("律师信息不存在");
        }
        return info;
    }

    public LawyerInfo submit(LawyerInfo info) {
        LawyerInfo existing = lawyerInfoMapper.selectOne(
                new LambdaQueryWrapper<LawyerInfo>().eq(LawyerInfo::getUserId, info.getUserId()));
        if (existing != null) {
            info.setId(existing.getId());
            info.setStatus(0);
            lawyerInfoMapper.updateById(info);
        } else {
            info.setStatus(0);
            lawyerInfoMapper.insert(info);
        }
        return info;
    }

    public LawyerInfo audit(Long id, int status) {
        LawyerInfo info = lawyerInfoMapper.selectById(id);
        if (info == null) {
            throw new BusinessException("律师信息不存在");
        }
        info.setStatus(status);
        lawyerInfoMapper.updateById(info);
        return info;
    }

    public PageResult<LawyerInfo> list(Integer status, PageParam pageParam) {
        LambdaQueryWrapper<LawyerInfo> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(LawyerInfo::getStatus, status);
        }
        wrapper.orderByDesc(LawyerInfo::getCreateTime);
        Page<LawyerInfo> page = lawyerInfoMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public Map<String, Object> count() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", lawyerInfoMapper.selectCount(null));
        result.put("certified", lawyerInfoMapper.selectCount(
                new LambdaQueryWrapper<LawyerInfo>().eq(LawyerInfo::getStatus, 1)));
        result.put("pending", lawyerInfoMapper.selectCount(
                new LambdaQueryWrapper<LawyerInfo>().eq(LawyerInfo::getStatus, 0)));
        return result;
    }
}
