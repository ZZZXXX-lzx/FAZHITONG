package com.fabaotong.consultation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.common.exception.BusinessException;
import com.fabaotong.consultation.entity.Consultation;
import com.fabaotong.consultation.mapper.ConsultationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationMapper consultationMapper;

    public Consultation create(Long userId, String title, String question, String type) {
        Consultation c = new Consultation();
        c.setUserId(userId);
        c.setTitle(title);
        c.setQuestion(question);
        c.setConsultationType(type);
        c.setStatus(0);
        consultationMapper.insert(c);
        return c;
    }

    public Consultation answer(Long id, Long lawyerId, String answer) {
        Consultation c = consultationMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("咨询记录不存在");
        }
        c.setLawyerId(lawyerId);
        c.setAnswer(answer);
        c.setStatus(1);
        c.setAnswerTime(LocalDateTime.now());
        consultationMapper.updateById(c);
        return c;
    }

    public PageResult<Consultation> listByUser(Long userId, PageParam pageParam) {
        Page<Consultation> page = consultationMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<Consultation>()
                        .eq(Consultation::getUserId, userId)
                        .orderByDesc(Consultation::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public PageResult<Consultation> listByLawyer(Long lawyerId, PageParam pageParam) {
        Page<Consultation> page = consultationMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<Consultation>()
                        .eq(Consultation::getLawyerId, lawyerId)
                        .orderByDesc(Consultation::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public PageResult<Consultation> listPending(PageParam pageParam) {
        Page<Consultation> page = consultationMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<Consultation>()
                        .eq(Consultation::getStatus, 0)
                        .orderByDesc(Consultation::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public Consultation getById(Long id) {
        return consultationMapper.selectById(id);
    }

    public PageResult<Consultation> listAll(PageParam pageParam) {
        Page<Consultation> page = consultationMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()),
                new LambdaQueryWrapper<Consultation>()
                        .orderByDesc(Consultation::getCreateTime));
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }
}
