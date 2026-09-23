package com.fazhitong.user.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.user.service.OperationLogService;
import com.fazhitong.user.service.SystemConfigService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user/system-config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;
    private final OperationLogService operationLogService;

    @GetMapping
    public ApiResult<Map<String, String>> get() {
        return ApiResult.success(systemConfigService.getMap());
    }

    @PutMapping
    public ApiResult<Void> save(@RequestBody Map<String, String> config,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {
        systemConfigService.saveAll(config);
        operationLogService.log(userId, username, "修改系统配置",
                OperationLogService.clientIp(request), "成功");
        return ApiResult.success();
    }
}