package com.fabaotong.auth.controller;

import com.fabaotong.auth.dto.LoginRequest;
import com.fabaotong.auth.dto.LoginResponse;
import com.fabaotong.auth.dto.RegisterRequest;
import com.fabaotong.auth.service.AuthService;
import com.fabaotong.common.dto.ApiResult;
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
