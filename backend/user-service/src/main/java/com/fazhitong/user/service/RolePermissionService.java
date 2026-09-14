package com.fazhitong.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fazhitong.common.exception.BusinessException;
import com.fazhitong.user.entity.Permission;
import com.fazhitong.user.entity.Role;
import com.fazhitong.user.entity.RolePermission;
import com.fazhitong.user.mapper.PermissionMapper;
import com.fazhitong.user.mapper.RoleMapper;
import com.fazhitong.user.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public List<Role> listRoles() {
        return roleMapper.selectList(null);
    }

    public Role createRole(Role role) {
        if (role.getRoleName() == null || role.getRoleName().isBlank()) {
            throw new BusinessException("角色名称不能为空");
        }
        long count = roleMapper.selectCount(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleName, role.getRoleName()));
        if (count > 0) throw new BusinessException("角色名称已存在");
        if (role.getStatus() == null) role.setStatus(1);
        roleMapper.insert(role);
        return role;
    }

    public Role updateRole(Role role) {
        if (role.getId() == null) throw new BusinessException("缺少角色ID");
        roleMapper.updateById(role);
        return roleMapper.selectById(role.getId());
    }

    @Transactional
    public void deleteRole(Long id) {
        if (id == null) return;
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));
        roleMapper.deleteById(id);
    }

    public List<Permission> listPermissions() {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getId));
    }

    /** 某角色的权限ID集合 */
    public List<Long> listPermissionIdsByRole(Long roleId) {
        return rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId))
                .stream().map(RolePermission::getPermissionId).toList();
    }

    /** 角色-权限 回显：{role, permissions:[...]} */
    public Map<String, Object> roleDetail(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) throw new BusinessException("角色不存在");
        List<Permission> perms = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getId));
        Map<String, Object> map = new HashMap<>();
        map.put("role", role);
        map.put("permissions", perms);
        map.put("checked", listPermissionIdsByRole(roleId));
        return map;
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) throw new BusinessException("角色不存在");
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        if (permissionIds != null) {
            for (Long pid : permissionIds) {
                if (pid == null) continue;
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(pid);
                rolePermissionMapper.insert(rp);
            }
        }
    }

    /** 按用户类型返回其可见服务模块的权限码；ADMIN 返回全部 */
    public List<String> listCodesByUserType(String userType) {
        String roleCode = switch (userType == null ? "" : userType) {
            case "LAWYER" -> "LAWYER";
            case "ENTERPRISE" -> "ENTERPRISE_ADMIN";
            case "USER" -> "USER";
            case "ADMIN" -> null;
            default -> "GUEST";
        };
        if (roleCode == null) { // ADMIN：全部权限码
            return permissionMapper.selectList(null)
                    .stream().map(Permission::getPermissionCode).toList();
        }
        Role role = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, roleCode));
        if (role == null) return List.of();
        List<Long> pids = rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, role.getId()))
                .stream().map(RolePermission::getPermissionId).toList();
        if (pids.isEmpty()) return List.of();
        return permissionMapper.selectBatchIds(pids)
                .stream().map(Permission::getPermissionCode).toList();
    }
}
