package com.fazhitong.casemgt.controller;

import com.fazhitong.casemgt.entity.Regulation;
import com.fazhitong.casemgt.service.RegulationService;
import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/case/regulation")
@RequiredArgsConstructor
public class RegulationController {

    private final RegulationService regulationService;

    @GetMapping("/search")
    public ApiResult<PageResult<Regulation>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lawType,
            PageParam pageParam) {
        return ApiResult.success(regulationService.search(keyword, lawType, pageParam));
    }

    @GetMapping("/{id}")
    public ApiResult<Regulation> getById(@PathVariable Long id) {
        return ApiResult.success(regulationService.getById(id));
    }
}
