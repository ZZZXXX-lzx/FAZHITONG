package com.fazhitong.casemgt.controller;

import com.fazhitong.casemgt.service.DueDiligenceService;
import com.fazhitong.common.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/case/due-diligence")
@RequiredArgsConstructor
public class DueDiligenceController {

    private final DueDiligenceService dueDiligenceService;

    /**
     * 生成尽调报告。body: { companyName, focus }
     */
    @PostMapping("/report")
    public ApiResult<Map<String, Object>> report(@RequestBody Map<String, String> body) {
        String companyName = body.getOrDefault("companyName", "");
        String focus = body.getOrDefault("focus", "");
        return ApiResult.success(dueDiligenceService.generate(companyName, focus));
    }
}
