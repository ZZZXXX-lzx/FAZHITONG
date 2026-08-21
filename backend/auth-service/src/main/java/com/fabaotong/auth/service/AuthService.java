package com.fabaotong.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fabaotong.auth.dto.LoginRequest;
import com.fabaotong.auth.dto.LoginResponse;
import com.fabaotong.auth.dto.RegisterRequest;
import com.fabaotong.auth.entity.User;
import com.fabaotong.auth.mapper.UserMapper;
import com.fabaotong.common.exception.BusinessException;
import com.fabaotong.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getAccount, req.getAccount()));
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        String token = JwtUtils.generate(user.getId(), user.getAccount(), user.getUserType());
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setNickname(user.getNickname());
        resp.setUserType(user.getUserType());
        return resp;
    }

    public LoginResponse register(RegisterRequest req) {
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getAccount, req.getAccount()));
        if (existing != null) {
            throw new BusinessException("账号已存在");
        }
        User user = new User();
        user.setAccount(req.getAccount());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname());
        user.setPhone(req.getPhone());
        user.setUserType(req.getUserType() != null ? req.getUserType() : "USER");
        user.setStatus(1);
        userMapper.insert(user);
        String token = JwtUtils.generate(user.getId(), user.getAccount(), user.getUserType());
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setNickname(user.getNickname());
        resp.setUserType(user.getUserType());
        return resp;
    }
}
