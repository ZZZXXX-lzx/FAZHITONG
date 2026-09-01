package com.fazhitong.casemgt.controller;

import com.fazhitong.casemgt.service.LitigationService;
import com.fazhitong.common.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/case/litigation")
@RequiredArgsConstructor
public class LitigationController {

    private final LitigationService litigationService;

    /**
     * 诉讼智能分析。body: { cause, description }
     */
    @PostMapping("/analyze")
    public ApiResult<Map<String, Object>> analyze(@RequestBody Map<String, String> body) {
        String cause = body.getOrDefault("cause", "");
        String description = body.getOrDefault("description", "");
        return ApiResult.success(litigationService.analyze(cause, description));
    }
}
