package com.fazhitong.consultation.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.consultation.entity.Consultation;
import com.fazhitong.consultation.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consultation")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping
    public ApiResult<Consultation> create(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String question,
            @RequestParam(defaultValue = "AI") String type) {
        return ApiResult.success(consultationService.create(userId, title, question, type));
    }

    @PostMapping("/{id}/answer")
    public ApiResult<Consultation> answer(
            @PathVariable Long id,
            @RequestParam Long lawyerId,
            @RequestParam String answer) {
        return ApiResult.success(consultationService.answer(id, lawyerId, answer));
    }

    @GetMapping("/my")
    public ApiResult<PageResult<Consultation>> myConsultations(
            @RequestParam Long userId, PageParam pageParam) {
        return ApiResult.success(consultationService.listByUser(userId, pageParam));
    }

    @GetMapping("/lawyer")
    public ApiResult<PageResult<Consultation>> lawyerConsultations(
            @RequestParam Long lawyerId, PageParam pageParam) {
        return ApiResult.success(consultationService.listByLawyer(lawyerId, pageParam));
    }

    @GetMapping("/pending")
    public ApiResult<PageResult<Consultation>> pending(PageParam pageParam) {
        return ApiResult.success(consultationService.listPending(pageParam));
    }

    @GetMapping("/all")
    public ApiResult<PageResult<Consultation>> all(PageParam pageParam) {
        return ApiResult.success(consultationService.listAll(pageParam));
    }

    @GetMapping("/{id}")
    public ApiResult<Consultation> getById(@PathVariable Long id) {
        return ApiResult.success(consultationService.getById(id));
    }
}
