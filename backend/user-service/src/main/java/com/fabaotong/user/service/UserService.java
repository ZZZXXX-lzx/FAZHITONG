package com.fabaotong.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fabaotong.common.dto.PageParam;
import com.fabaotong.common.dto.PageResult;
import com.fabaotong.common.exception.BusinessException;
import com.fabaotong.user.entity.User;
import com.fabaotong.user.entity.Role;
import com.fabaotong.user.mapper.UserMapper;
import com.fabaotong.user.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }

    public PageResult<User> listUsers(String keyword, String userType, PageParam pageParam) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null) {
            wrapper.like(User::getNickname, keyword)
                    .or().like(User::getAccount, keyword)
                    .or().like(User::getPhone, keyword);
        }
        if (userType != null) {
            wrapper.eq(User::getUserType, userType);
        }
        Page<User> page = userMapper.selectPage(
                new Page<>(pageParam.getPage(), pageParam.getSize()), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public User create(User user) {
        long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getAccount, user.getAccount()));
        if (count > 0) throw new BusinessException("账号已存在");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return user;
    }

    public User update(User user) {
        userMapper.updateById(user);
        return userMapper.selectById(user.getId());
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    public List<Role> listRoles() {
        return roleMapper.selectList(null);
    }

    public long countAll() {
        return userMapper.selectCount(null);
    }

    public long countByType(String userType) {
        return userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUserType, userType));
    }
}
