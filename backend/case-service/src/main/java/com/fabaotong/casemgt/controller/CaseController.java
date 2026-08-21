package com.fabaotong.casemgt.controller;

import com.fabaotong.common.dto.ApiResult;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.casemgt.entity.CaseGovernment;
import com.fabaotong.casemgt.service.CaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/case")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    @GetMapping("/search")
    public ApiResult<PageResult<CaseGovernment>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String causeName,
            @RequestParam(required = false) String courtName,
            @RequestParam(required = false) String caseYear,
            PageParam pageParam) {
        return ApiResult.success(
                caseService.search(keyword, causeName, courtName, caseYear, pageParam));
    }

    @GetMapping("/{id}")
    public ApiResult<CaseGovernment> getById(@PathVariable Long id) {
        return ApiResult.success(caseService.getById(id));
    }
}

