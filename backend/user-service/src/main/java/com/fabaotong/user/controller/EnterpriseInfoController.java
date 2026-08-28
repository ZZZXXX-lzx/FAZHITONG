package com.fabaotong.user.controller;

import com.fabaotong.common.dto.ApiResult;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.user.entity.EnterpriseInfo;
import com.fabaotong.user.service.EnterpriseInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/enterprise")
@RequiredArgsConstructor
public class EnterpriseInfoController {

    private final EnterpriseInfoService enterpriseInfoService;

    @GetMapping("/info/{userId}")
    public ApiResult<EnterpriseInfo> getByUserId(@PathVariable Long userId) {
        return ApiResult.success(enterpriseInfoService.getByUserId(userId));
    }

    @PostMapping("/submit")
    public ApiResult<EnterpriseInfo> submit(@RequestBody EnterpriseInfo info) {
        return ApiResult.success(enterpriseInfoService.submit(info));
    }

    @PostMapping("/audit/{id}")
    public ApiResult<EnterpriseInfo> audit(@PathVariable Long id, @RequestParam int status) {
        return ApiResult.success(enterpriseInfoService.audit(id, status));
    }

    @GetMapping("/list")
    public ApiResult<PageResult<EnterpriseInfo>> list(
            @RequestParam(required = false) Integer status,
            PageParam pageParam) {
        return ApiResult.success(enterpriseInfoService.list(status, pageParam));
    }
}
