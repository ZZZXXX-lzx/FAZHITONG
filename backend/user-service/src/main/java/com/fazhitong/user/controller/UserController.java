package com.fazhitong.user.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.common.dto.PageParam;
import com.fazhitong.common.dto.PageResult;
import com.fazhitong.user.entity.User;
import com.fazhitong.user.entity.Role;
import com.fazhitong.user.service.UserService;
import com.fazhitong.user.service.LawyerInfoService;
import com.fazhitong.user.service.EnterpriseInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LawyerInfoService lawyerInfoService;
    private final EnterpriseInfoService enterpriseInfoService;

    @GetMapping("/{id}")
    public ApiResult<User> getById(@PathVariable Long id) {
        return ApiResult.success(userService.getById(id));
    }

    @GetMapping("/list")
    public ApiResult<PageResult<User>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String userType,
            PageParam pageParam) {
        return ApiResult.success(userService.listUsers(keyword, userType, pageParam));
    }

    @PostMapping
    public ApiResult<User> create(@RequestBody User user) {
        return ApiResult.success(userService.create(user));
    }

    @PutMapping
    public ApiResult<User> update(@RequestBody User user) {
        return ApiResult.success(userService.update(user));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResult.success();
    }

    @GetMapping("/roles")
    public ApiResult<List<Role>> listRoles() {
        return ApiResult.success(userService.listRoles());
    }

    @GetMapping("/dashboard/stats")
    public ApiResult<Map<String, Object>> dashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.countAll());
        stats.put("lawyers", userService.countByType("LAWYER"));
        stats.put("enterprises", userService.countByType("ENTERPRISE"));
        stats.put("adminUsers", userService.countByType("ADMIN"));
        stats.put("lawyerInfo", lawyerInfoService.count());
        stats.put("enterpriseInfo", enterpriseInfoService.count());
        return ApiResult.success(stats);
    }
}
