package com.fazhitong.contract.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.contract.entity.ContractRecord;
import com.fazhitong.contract.entity.ContractReviewRisk;
import com.fazhitong.contract.entity.EnterpriseContract;
import com.fazhitong.contract.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping("/upload")
    public ApiResult<ContractRecord> upload(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long enterpriseId,
            @RequestParam String title,
            @RequestParam String fileUrl) {
        return ApiResult.success(contractService.uploadReview(userId, enterpriseId, title, fileUrl));
    }

    @PostMapping("/{id}/review")
    public ApiResult<ContractRecord> review(
            @PathVariable Long id,
            @RequestParam String riskReport,
            @RequestParam String riskLevel) {
        return ApiResult.success(contractService.reviewResult(id, riskReport, riskLevel));
    }

    @GetMapping("/records")
    public ApiResult<PageResult<ContractRecord>> listRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long enterpriseId,
            PageParam pageParam) {
        return ApiResult.success(contractService.listRecords(userId, enterpriseId, pageParam));
    }

    @PostMapping("/enterprise")
    public ApiResult<EnterpriseContract> createEnterpriseContract(@RequestBody EnterpriseContract contract) {
        return ApiResult.success(contractService.createEnterpriseContract(contract));
    }

    @GetMapping("/enterprise/list")
    public ApiResult<PageResult<EnterpriseContract>> listEnterpriseContracts(
            @RequestParam Long enterpriseId, PageParam pageParam) {
        return ApiResult.success(contractService.listEnterpriseContracts(enterpriseId, pageParam));
    }

    /**
     * 合同状态流转（提交签署 / 签署 / 归档 / 作废）
     */
    @PostMapping("/enterprise/{id}/transition")
    public ApiResult<EnterpriseContract> transition(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam(required = false) String signerName) {
        return ApiResult.success(contractService.transition(id, action, signerName));
    }

    /**
     * 到期预警：查询距离到期不足 days 天的合同
     */
    @GetMapping("/enterprise/expiring")
    public ApiResult<List<EnterpriseContract>> listExpiring(
            @RequestParam Long enterpriseId,
            @RequestParam(defaultValue = "30") int days) {
        return ApiResult.success(contractService.listExpiring(enterpriseId, days));
    }

    /**
     * 合同智能审查（传入合同文本，返回结构化风险点清单 + 风险等级）
     */
    @PostMapping("/ai-review")
    public ApiResult<Map<String, Object>> aiReview(@RequestBody Map<String, Object> body) {
        String text = body.get("text") == null ? "" : body.get("text").toString();
        String dimension = body.get("dimension") == null ? "GENERAL" : body.get("dimension").toString();
        Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
        Long enterpriseId = body.get("enterpriseId") == null ? null : Long.valueOf(body.get("enterpriseId").toString());
        if (text == null || text.isBlank()) {
            throw new BusinessException("请提供待审查的合同文本");
        }
        return ApiResult.success(contractService.aiReview(text, dimension, userId, enterpriseId));
    }

    /**
     * 查询审查任务的风险点清单
     */
    @GetMapping("/ai-review/{taskId}/risks")
    public ApiResult<List<ContractReviewRisk>> listRisks(@PathVariable Long taskId) {
        return ApiResult.success(contractService.listRisks(taskId));
    }
}
