package com.fabaotong.contract.controller;

import com.fabaotong.common.dto.ApiResult;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.contract.entity.ContractRecord;
import com.fabaotong.contract.entity.EnterpriseContract;
import com.fabaotong.contract.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
