package com.fazhitong.consultation.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.consultation.entity.LawyerInfo;
import com.fazhitong.consultation.entity.LawyerService;
import com.fazhitong.consultation.entity.LawyerServicePrice;
import com.fazhitong.consultation.service.LawyerServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultation/lawyer-service")
@RequiredArgsConstructor
public class LawyerServiceController {

    private final LawyerServiceService lawyerServiceService;

    /**
     * 创建委托
     */
    @PostMapping
    public ApiResult<LawyerService> create(@RequestBody LawyerService lawyerService) {
        return ApiResult.success(lawyerServiceService.create(lawyerService));
    }

    /**
     * 我的委托
     */
    @GetMapping("/my")
    public ApiResult<PageResult<LawyerService>> my(
            @RequestParam Long userId, PageParam pageParam) {
        return ApiResult.success(lawyerServiceService.my(userId, pageParam));
    }

    /**
     * 律师收到的委托
     */
    @GetMapping("/lawyer")
    public ApiResult<PageResult<LawyerService>> lawyerCases(
            @RequestParam Long lawyerId,
            @RequestParam(required = false) Integer status,
            PageParam pageParam) {
        return ApiResult.success(
                lawyerServiceService.lawyerCases(lawyerId, status, pageParam));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public ApiResult<LawyerService> getById(@PathVariable Long id) {
        return ApiResult.success(lawyerServiceService.getById(id));
    }

    /**
     * 接单
     */
    @PostMapping("/{id}/accept")
    public ApiResult<LawyerService> accept(
            @PathVariable Long id,
            @RequestParam String lawyerRemark) {
        return ApiResult.success(lawyerServiceService.accept(id, lawyerRemark));
    }

    /**
     * 开始服务
     */
    @PostMapping("/{id}/start")
    public ApiResult<LawyerService> start(@PathVariable Long id) {
        return ApiResult.success(lawyerServiceService.startWork(id));
    }

    /**
     * 完成
     */
    @PostMapping("/{id}/finish")
    public ApiResult<LawyerService> finish(@PathVariable Long id) {
        return ApiResult.success(lawyerServiceService.finish(id));
    }

    /**
     * 取消
     */
    @PostMapping("/{id}/cancel")
    public ApiResult<LawyerService> cancel(@PathVariable Long id) {
        return ApiResult.success(lawyerServiceService.cancel(id));
    }

    /**
     * 查律师价格
     */
    @GetMapping("/prices/{lawyerId}")
    public ApiResult<List<LawyerServicePrice>> prices(@PathVariable Long lawyerId) {
        return ApiResult.success(lawyerServiceService.getPrices(lawyerId));
    }

    /**
     * 设置价格
     */
    @PostMapping("/prices")
    public ApiResult<LawyerServicePrice> setPrice(@RequestBody LawyerServicePrice price) {
        return ApiResult.success(lawyerServiceService.setPrice(price));
    }

    /**
     * 律师大厅
     */
    @GetMapping("/lawyers")
    public ApiResult<PageResult<LawyerInfo>> lawyers(
            @RequestParam(required = false) String keyword,
            PageParam pageParam) {
        return ApiResult.success(lawyerServiceService.listLawyers(keyword, pageParam));
    }
}
