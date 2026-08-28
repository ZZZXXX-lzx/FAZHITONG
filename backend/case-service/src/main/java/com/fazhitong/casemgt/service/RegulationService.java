package com.fazhitong.casemgt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.casemgt.entity.Regulation;
import com.fazhitong.casemgt.mapper.RegulationMapper;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegulationService {

    private final RegulationMapper regulationMapper;

    public PageResult<Regulation> search(String keyword, String lawType, PageParam pageParam) {
        LambdaQueryWrapper<Regulation> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Regulation::getTitle, keyword)
                    .or().like(Regulation::getKeywords, keyword)
                    .or().like(Regulation::getContent, keyword));
        }
        if (lawType != null && !lawType.isBlank()) {
            wrapper.eq(Regulation::getLawType, lawType);
        }
        wrapper.orderByDesc(Regulation::getPublishDate).orderByDesc(Regulation::getId);
        Page<Regulation> page = regulationMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public Regulation getById(Long id) {
        return regulationMapper.selectById(id);
    }
}
