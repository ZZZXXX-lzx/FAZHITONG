package com.fazhitong.auth.controller;

import com.fazhitong.auth.dto.LoginRequest;
import com.fazhitong.auth.dto.LoginResponse;
import com.fazhitong.auth.dto.RegisterRequest;
import com.fazhitong.auth.service.AuthService;
import com.fazhitong.common.dto.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResult.success(authService.login(req));
    }

    @PostMapping("/register")
    public ApiResult<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResult.success(authService.register(req));
    }
}
