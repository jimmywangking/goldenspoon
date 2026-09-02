package com.example.crm.userauth.module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.module.entity.Role;
import com.example.crm.userauth.module.entity.RolePagePermission;
import com.example.crm.userauth.module.mapper.RoleMapper;
import com.example.crm.userauth.module.mapper.RolePagePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleMapper roleMapper;
    private final RolePagePermissionMapper rolePagePermissionMapper;

    public Page<Role> list(int page, int size, String keyword) {
        Page<Role> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Role::getName, keyword).or().like(Role::getCode, keyword);
        }
        wrapper.orderByDesc(Role::getCreatedAt);
        return roleMapper.selectPage(pageParam, wrapper);
    }

    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }

    public Role create(Role role) {
        if (role.getIsSystem() == null) role.setIsSystem(false);
        roleMapper.insert(role);
        return role;
    }

    public void update(Role role) {
        Role existing = roleMapper.selectById(role.getId());
        if (existing == null) throw new IllegalArgumentException("角色不存在");
        if (existing.getIsSystem()) throw new IllegalArgumentException("系统角色不可修改");
        roleMapper.updateById(role);
    }

    public void delete(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) throw new IllegalArgumentException("角色不存在");
        if (role.getIsSystem()) throw new IllegalArgumentException("系统角色不可删除");
        roleMapper.deleteById(id);
    }

    public List<RolePagePermission> getPermissions(Long roleId) {
        LambdaQueryWrapper<RolePagePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePagePermission::getRoleId, roleId);
        return rolePagePermissionMapper.selectList(wrapper);
    }

    public void setPermissions(Long roleId, List<RolePagePermission> permissions) {
        LambdaQueryWrapper<RolePagePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePagePermission::getRoleId, roleId);
        rolePagePermissionMapper.delete(wrapper);
        for (RolePagePermission p : permissions) {
            p.setRoleId(roleId);
            rolePagePermissionMapper.insert(p);
        }
    }
}
