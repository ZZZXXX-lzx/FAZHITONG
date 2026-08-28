package com.fazhitong.payment.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.payment.entity.Order;
import com.fazhitong.payment.entity.Member;
import com.fazhitong.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/order")
    public ApiResult<Order> createOrder(
            @RequestParam Long userId,
            @RequestParam String orderType,
            @RequestParam BigDecimal amount) {
        return ApiResult.success(paymentService.createOrder(userId, orderType, amount));
    }

    @PostMapping("/pay/{orderId}")
    public ApiResult<Order> pay(@PathVariable Long orderId) {
        return ApiResult.success(paymentService.pay(orderId));
    }

    @GetMapping("/orders")
    public ApiResult<PageResult<Order>> listOrders(
            @RequestParam Long userId, PageParam pageParam) {
        return ApiResult.success(paymentService.listOrders(userId, pageParam));
    }

    @GetMapping("/admin/orders")
    public ApiResult<PageResult<Order>> listAllOrders(PageParam pageParam) {
        return ApiResult.success(paymentService.listAllOrders(pageParam));
    }

    @GetMapping("/member")
    public ApiResult<Member> getMember(@RequestParam Long userId) {
        return ApiResult.success(paymentService.getMember(userId));
    }
}
