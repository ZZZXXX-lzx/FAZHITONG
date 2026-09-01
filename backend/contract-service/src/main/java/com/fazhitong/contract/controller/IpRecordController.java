package com.fazhitong.contract.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.contract.entity.IpRecord;
import com.fazhitong.contract.service.IpRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contract/ip")
@RequiredArgsConstructor
public class IpRecordController {

    private final IpRecordService ipRecordService;

    @PostMapping
    public ApiResult<IpRecord> create(@RequestBody IpRecord record) {
        return ApiResult.success(ipRecordService.create(record));
    }

    @GetMapping("/list")
    public ApiResult<PageResult<IpRecord>> list(
            @RequestParam(required = false) Long enterpriseId,
            @RequestParam(required = false) String ipType,
            @RequestParam(required = false) String status,
            PageParam pageParam) {
        return ApiResult.success(ipRecordService.list(enterpriseId, ipType, status, pageParam));
    }

    @PutMapping
    public ApiResult<IpRecord> update(@RequestBody IpRecord record) {
        return ApiResult.success(ipRecordService.update(record));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        ipRecordService.delete(id);
        return ApiResult.success();
    }

    @GetMapping("/expiring")
    public ApiResult<List<IpRecord>> listExpiring(
            @RequestParam Long enterpriseId,
            @RequestParam(defaultValue = "90") int days) {
        return ApiResult.success(ipRecordService.listExpiring(enterpriseId, days));
    }
}
