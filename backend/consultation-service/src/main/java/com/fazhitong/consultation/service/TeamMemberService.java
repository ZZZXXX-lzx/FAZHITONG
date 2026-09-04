package com.fazhitong.consultation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.consultation.entity.TeamMember;
import com.fazhitong.consultation.mapper.TeamMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberMapper teamMemberMapper;

    public TeamMember create(TeamMember m) {
        if (m.getStatus() == null || m.getStatus().isBlank()) {
            m.setStatus("ACTIVE");
        }
        if (m.getRole() == null || m.getRole().isBlank()) {
            m.setRole("LAWYER");
        }
        teamMemberMapper.insert(m);
        return m;
    }

    public PageResult<TeamMember> list(Long lawyerId, String keyword, String status, PageParam pageParam) {
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        if (lawyerId != null) wrapper.eq(TeamMember::getLawyerId, lawyerId);
        if (status != null && !status.isBlank()) wrapper.eq(TeamMember::getStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(TeamMember::getName, keyword)
                    .or().like(TeamMember::getPhone, keyword)
                    .or().like(TeamMember::getEmail, keyword));
        }
        wrapper.orderByDesc(TeamMember::getCreateTime);
        Page<TeamMember> page = teamMemberMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public TeamMember update(TeamMember m) {
        TeamMember existing = teamMemberMapper.selectById(m.getId());
        if (existing == null) {
            throw new BusinessException("团队成员不存在");
        }
        teamMemberMapper.updateById(m);
        return teamMemberMapper.selectById(m.getId());
    }

    public void delete(Long id) {
        teamMemberMapper.deleteById(id);
    }
}
