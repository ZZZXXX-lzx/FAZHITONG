package com.fazhitong.contract.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.contract.entity.InvestmentRecord;
import com.fazhitong.contract.service.InvestmentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contract/investment")
@RequiredArgsConstructor
public class InvestmentRecordController {

    private final InvestmentRecordService investmentRecordService;

    @PostMapping
    public ApiResult<InvestmentRecord> create(@RequestBody InvestmentRecord record) {
        return ApiResult.success(investmentRecordService.create(record));
    }

    @GetMapping("/list")
    public ApiResult<PageResult<InvestmentRecord>> list(
            @RequestParam(required = false) Long enterpriseId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            PageParam pageParam) {
        return ApiResult.success(investmentRecordService.list(enterpriseId, type, status, pageParam));
    }

    @PutMapping
    public ApiResult<InvestmentRecord> update(@RequestBody InvestmentRecord record) {
        return ApiResult.success(investmentRecordService.update(record));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        investmentRecordService.delete(id);
        return ApiResult.success();
    }
}
