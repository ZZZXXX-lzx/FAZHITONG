package com.fazhitong.user.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.user.entity.Permission;
import com.fazhitong.user.entity.Role;
import com.fazhitong.user.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/role")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @PostMapping
    public ApiResult<Role> create(@RequestBody Role role) {
        return ApiResult.success(rolePermissionService.createRole(role));
    }

    @PutMapping
    public ApiResult<Role> update(@RequestBody Role role) {
        return ApiResult.success(rolePermissionService.updateRole(role));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        rolePermissionService.deleteRole(id);
        return ApiResult.success();
    }

    @GetMapping("/permissions")
    public ApiResult<List<Permission>> permissions() {
        return ApiResult.success(rolePermissionService.listPermissions());
    }

    @GetMapping("/{id}")
    public ApiResult<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResult.success(rolePermissionService.roleDetail(id));
    }

    @PutMapping("/{id}/permissions")
    public ApiResult<Void> assign(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        rolePermissionService.assignPermissions(id, permissionIds);
        return ApiResult.success();
    }

    @GetMapping("/perms")
    public ApiResult<List<String>> perms(
            @RequestHeader(value = "X-User-Type", required = false) String userType) {
        return ApiResult.success(rolePermissionService.listCodesByUserType(userType));
    }
}
