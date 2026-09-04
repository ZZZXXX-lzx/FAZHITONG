package com.fazhitong.consultation.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.consultation.entity.TeamMember;
import com.fazhitong.consultation.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consultation/team")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @PostMapping
    public ApiResult<TeamMember> create(@RequestBody TeamMember m) {
        return ApiResult.success(teamMemberService.create(m));
    }

    @GetMapping("/list")
    public ApiResult<PageResult<TeamMember>> list(
            @RequestParam(required = false) Long lawyerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            PageParam pageParam) {
        return ApiResult.success(teamMemberService.list(lawyerId, keyword, status, pageParam));
    }

    @PutMapping
    public ApiResult<TeamMember> update(@RequestBody TeamMember m) {
        return ApiResult.success(teamMemberService.update(m));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        teamMemberService.delete(id);
        return ApiResult.success();
    }
}
