package com.fazhitong.casemgt.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.casemgt.entity.CaseGovernment;
import com.fazhitong.casemgt.service.CaseService;
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
            @RequestParam(required = false) String lawArticle,
            @RequestParam(required = false) String courtLevel,
            PageParam pageParam) {
        return ApiResult.success(
                caseService.search(keyword, causeName, courtName, caseYear, lawArticle, courtLevel, pageParam));
    }

    @GetMapping("/{id}")
    public ApiResult<CaseGovernment> getById(@PathVariable Long id) {
        return ApiResult.success(caseService.getById(id));
    }
}

