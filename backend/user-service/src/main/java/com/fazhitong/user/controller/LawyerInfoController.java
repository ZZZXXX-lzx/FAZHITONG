package com.fazhitong.user.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.user.entity.LawyerInfo;
import com.fazhitong.user.service.LawyerInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/lawyer")
@RequiredArgsConstructor
public class LawyerInfoController {

    private final LawyerInfoService lawyerInfoService;

    @GetMapping("/info/{userId}")
    public ApiResult<LawyerInfo> getByUserId(@PathVariable Long userId) {
        return ApiResult.success(lawyerInfoService.getByUserId(userId));
    }

    @PostMapping("/submit")
    public ApiResult<LawyerInfo> submit(@RequestBody LawyerInfo info) {
        return ApiResult.success(lawyerInfoService.submit(info));
    }

    @PostMapping("/audit/{id}")
    public ApiResult<LawyerInfo> audit(@PathVariable Long id, @RequestParam int status) {
        return ApiResult.success(lawyerInfoService.audit(id, status));
    }

    @GetMapping("/list")
    public ApiResult<PageResult<LawyerInfo>> list(
            @RequestParam(required = false) Integer status,
            PageParam pageParam) {
        return ApiResult.success(lawyerInfoService.list(status, pageParam));
    }
}
