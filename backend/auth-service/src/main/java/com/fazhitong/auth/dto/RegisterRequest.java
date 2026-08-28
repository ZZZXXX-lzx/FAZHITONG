package com.fazhitong.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 32, message = "账号长度需在3-32个字符之间")
    private String account;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在6-64个字符之间")
    private String password;
    private String nickname;
    private String phone;
    private String userType;
}
