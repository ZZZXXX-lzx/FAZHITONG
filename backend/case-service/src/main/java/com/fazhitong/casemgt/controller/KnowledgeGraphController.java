package com.fazhitong.casemgt.controller;

import com.fazhitong.casemgt.service.KnowledgeGraphService;
import com.fazhitong.common.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/case/kg")
@RequiredArgsConstructor
public class KnowledgeGraphController {

    private final KnowledgeGraphService kgService;

    /** 完整知识图谱（可按领域过滤） */
    @GetMapping("/graph")
    public ApiResult<Map<String, Object>> graph(@RequestParam(required = false) String domain) {
        return ApiResult.success(kgService.graph(domain));
    }

    /** 领域列表 */
    @GetMapping("/domains")
    public ApiResult<List<String>> domains() {
        return ApiResult.success(kgService.domains());
    }

    /** 按关键词查询关联法规与概念 */
    @GetMapping("/related")
    public ApiResult<Map<String, Object>> related(@RequestParam String keyword) {
        return ApiResult.success(kgService.related(keyword));
    }
}
