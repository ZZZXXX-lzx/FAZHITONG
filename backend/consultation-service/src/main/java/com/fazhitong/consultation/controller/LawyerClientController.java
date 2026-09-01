package com.fazhitong.consultation.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.consultation.entity.LawyerClient;
import com.fazhitong.consultation.service.LawyerClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consultation/client")
@RequiredArgsConstructor
public class LawyerClientController {

    private final LawyerClientService clientService;

    @PostMapping
    public ApiResult<LawyerClient> create(@RequestBody LawyerClient c) {
        return ApiResult.success(clientService.create(c));
    }

    @GetMapping("/list")
    public ApiResult<PageResult<LawyerClient>> list(
            @RequestParam(required = false) Long lawyerId,
            @RequestParam(required = false) String keyword,
            PageParam pageParam) {
        return ApiResult.success(clientService.list(lawyerId, keyword, pageParam));
    }

    @PutMapping
    public ApiResult<LawyerClient> update(@RequestBody LawyerClient c) {
        return ApiResult.success(clientService.update(c));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ApiResult.success();
    }
}
