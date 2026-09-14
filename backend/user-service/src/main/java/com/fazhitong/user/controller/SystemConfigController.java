package com.fazhitong.user.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.user.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user/system-config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public ApiResult<Map<String, String>> get() {
        return ApiResult.success(systemConfigService.getMap());
    }

    @PutMapping
    public ApiResult<Void> save(@RequestBody Map<String, String> config) {
        systemConfigService.saveAll(config);
        return ApiResult.success();
    }
}