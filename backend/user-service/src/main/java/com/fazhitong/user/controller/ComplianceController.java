package com.fazhitong.user.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.user.service.ComplianceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/compliance")
@RequiredArgsConstructor
public class ComplianceController {

    private final ComplianceService complianceService;

    /**
     * 获取问卷（含已有草稿答案）
     */
    @GetMapping("/questions")
    public ApiResult<Map<String, Object>> questions(@RequestParam(required = false) Long enterpriseId) {
        return ApiResult.success(complianceService.getQuestions(enterpriseId));
    }

    /**
     * 提交答案。body: { enterpriseId, answers:[{questionId, answer}], submit }
     */
    @PostMapping("/answers")
    public ApiResult<Map<String, Object>> submitAnswers(@RequestBody Map<String, Object> body) {
        Long enterpriseId = body.get("enterpriseId") == null ? null : Long.valueOf(body.get("enterpriseId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, String>> answers = (List<Map<String, String>>) body.get("answers");
        boolean submit = Boolean.TRUE.equals(body.get("submit"));
        return ApiResult.success(complianceService.submitAnswers(enterpriseId, answers, submit));
    }

    /**
     * 获取最近一次报告
     */
    @GetMapping("/report")
    public ApiResult<Map<String, Object>> report(@RequestParam Long enterpriseId) {
        return ApiResult.success(complianceService.latestReport(enterpriseId));
    }
}
