package com.fazhitong.casemgt.controller;

import com.fazhitong.casemgt.service.KbService;
import com.fazhitong.common.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/case/kb")
@RequiredArgsConstructor
public class KbController {

    private final KbService kbService;

    /**
     * 知识检索。body: { query, topK }
     */
    @PostMapping("/retrieve")
    public ApiResult<Map<String, Object>> retrieve(@RequestBody Map<String, Object> body) {
        String query = body.get("query") == null ? "" : body.get("query").toString();
        int topK = body.get("topK") == null ? 5 : Integer.parseInt(body.get("topK").toString());
        return ApiResult.success(kbService.retrieve(query, topK));
    }
}
