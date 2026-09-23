package com.fazhitong.user.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.user.entity.OperationLog;
import com.fazhitong.user.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/operation-log")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping("/list")
    public ApiResult<PageResult<OperationLog>> list(PageParam pageParam,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String result) {
        return ApiResult.success(operationLogService.pageList(pageParam, keyword, result));
    }
}
