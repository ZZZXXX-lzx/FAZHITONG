package com.fazhitong.consultation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.consultation.entity.LawyerInfo;
import com.fazhitong.consultation.entity.LawyerService;
import com.fazhitong.consultation.entity.LawyerServicePrice;
import com.fazhitong.consultation.mapper.LawyerInfoMapper;
import com.fazhitong.consultation.mapper.LawyerServiceMapper;
import com.fazhitong.consultation.mapper.LawyerServicePriceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LawyerServiceService {

    private final LawyerServiceMapper lawyerServiceMapper;
    private final LawyerServicePriceMapper lawyerServicePriceMapper;
    private final LawyerInfoMapper lawyerInfoMapper;

    /**
     * 委托创建(默认status=0)
     */
    public LawyerService create(LawyerService lawyerService) {
        lawyerService.setStatus(0);
        lawyerServiceMapper.insert(lawyerService);
        return lawyerService;
    }

    /**
     * 我的委托(分页)
     */
    public PageResult<LawyerService> my(Long userId, PageParam pageParam) {
        Page<LawyerService> page = lawyerServiceMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<LawyerService>()
                        .eq(LawyerService::getUserId, userId)
                        .orderByDesc(LawyerService::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 律师收到的委托(分页, status可选)
     */
    public PageResult<LawyerService> lawyerCases(Long lawyerId, Integer status, PageParam pageParam) {
        LambdaQueryWrapper<LawyerService> wrapper = new LambdaQueryWrapper<LawyerService>()
                .eq(LawyerService::getLawyerId, lawyerId);
        if (status != null) {
            wrapper.eq(LawyerService::getStatus, status);
        }
        wrapper.orderByDesc(LawyerService::getCreateTime);
        Page<LawyerService> page = lawyerServiceMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 详情
     */
    public LawyerService getById(Long id) {
        LawyerService lawyerService = lawyerServiceMapper.selectById(id);
        if (lawyerService == null) {
            throw new BusinessException("委托不存在");
        }
        return lawyerService;
    }

    /**
     * 律师接单(status=1, lawyerRemark, acceptTime=now)
     */
    public LawyerService accept(Long id, String lawyerRemark) {
        LawyerService lawyerService = lawyerServiceMapper.selectById(id);
        if (lawyerService == null) {
            throw new BusinessException("委托不存在");
        }
        lawyerService.setStatus(1);
        lawyerService.setLawyerRemark(lawyerRemark);
        lawyerService.setAcceptTime(LocalDateTime.now());
        lawyerServiceMapper.updateById(lawyerService);
        return lawyerService;
    }

    /**
     * 开始服务(status=2)
     */
    public LawyerService startWork(Long id) {
        LawyerService lawyerService = lawyerServiceMapper.selectById(id);
        if (lawyerService == null) {
            throw new BusinessException("委托不存在");
        }
        lawyerService.setStatus(2);
        lawyerServiceMapper.updateById(lawyerService);
        return lawyerService;
    }

    /**
     * 完成(status=3, finishTime=now)
     */
    public LawyerService finish(Long id) {
        LawyerService lawyerService = lawyerServiceMapper.selectById(id);
        if (lawyerService == null) {
            throw new BusinessException("委托不存在");
        }
        lawyerService.setStatus(3);
        lawyerService.setFinishTime(LocalDateTime.now());
        lawyerServiceMapper.updateById(lawyerService);
        return lawyerService;
    }

    /**
     * 取消(status=5)
     */
    public LawyerService cancel(Long id) {
        LawyerService lawyerService = lawyerServiceMapper.selectById(id);
        if (lawyerService == null) {
            throw new BusinessException("委托不存在");
        }
        lawyerService.setStatus(5);
        lawyerServiceMapper.updateById(lawyerService);
        return lawyerService;
    }

    /**
     * 查询律师服务价格
     */
    public List<LawyerServicePrice> getPrices(Long lawyerId) {
        return lawyerServicePriceMapper.selectList(
                new LambdaQueryWrapper<LawyerServicePrice>()
                        .eq(LawyerServicePrice::getLawyerId, lawyerId)
                        .eq(LawyerServicePrice::getStatus, 1)
                        .orderByDesc(LawyerServicePrice::getCreateTime));
    }

    /**
     * 设置/更新价格
     */
    public LawyerServicePrice setPrice(LawyerServicePrice price) {
        if (price.getStatus() == null) {
            price.setStatus(1);
        }
        if (price.getId() != null) {
            LawyerServicePrice existing = lawyerServicePriceMapper.selectById(price.getId());
            if (existing == null) {
                throw new BusinessException("价格记录不存在");
            }
            lawyerServicePriceMapper.updateById(price);
        } else {
            lawyerServicePriceMapper.insert(price);
        }
        return price;
    }

    /**
     * 查询已认证律师(关联lawyer_info表status=1, 模糊搜索specialty/lawFirm/description)
     */
    public PageResult<LawyerInfo> listLawyers(String keyword, PageParam pageParam) {
        LambdaQueryWrapper<LawyerInfo> wrapper = new LambdaQueryWrapper<LawyerInfo>()
                .eq(LawyerInfo::getStatus, 1);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(LawyerInfo::getSpecialty, keyword)
                    .or().like(LawyerInfo::getLawFirm, keyword)
                    .or().like(LawyerInfo::getDescription, keyword));
        }
        wrapper.orderByDesc(LawyerInfo::getId);
        Page<LawyerInfo> page = lawyerInfoMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }
}
