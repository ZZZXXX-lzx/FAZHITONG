package com.fazhitong.user.controller;

import com.fazhitong.common.dto.ApiResult;
import com.fazhitong.user.entity.Permission;
import com.fazhitong.user.entity.Role;
import com.fazhitong.user.service.OperationLogService;
import com.fazhitong.user.service.RolePermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/role")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;
    private final OperationLogService operationLogService;

    @PostMapping
    public ApiResult<Role> create(@RequestBody Role role,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {
        Role r = rolePermissionService.createRole(role);
        operationLogService.log(userId, username, "新增角色 " + role.getRoleName(),
                OperationLogService.clientIp(request), "成功");
        return ApiResult.success(r);
    }

    @PutMapping
    public ApiResult<Role> update(@RequestBody Role role,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {
        Role r = rolePermissionService.updateRole(role);
        String name = role.getRoleName() != null ? role.getRoleName() : "ID=" + role.getId();
        operationLogService.log(userId, username, "修改角色 " + name,
                OperationLogService.clientIp(request), "成功");
        return ApiResult.success(r);
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {
        rolePermissionService.deleteRole(id);
        operationLogService.log(userId, username, "删除角色 ID=" + id,
                OperationLogService.clientIp(request), "成功");
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
    public ApiResult<Void> assign(@PathVariable Long id, @RequestBody List<Long> permissionIds,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            HttpServletRequest request) {
        rolePermissionService.assignPermissions(id, permissionIds);
        operationLogService.log(userId, username, "分配角色权限 ID=" + id,
                OperationLogService.clientIp(request), "成功");
        return ApiResult.success();
    }

    @GetMapping("/perms")
    public ApiResult<List<String>> perms(
            @RequestHeader(value = "X-User-Type", required = false) String userType) {
        return ApiResult.success(rolePermissionService.listCodesByUserType(userType));
    }
}
