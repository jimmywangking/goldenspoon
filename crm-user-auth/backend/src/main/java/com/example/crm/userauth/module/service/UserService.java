package com.example.crm.userauth.module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.module.dto.CreateUserRequest;
import com.example.crm.userauth.module.dto.UserDetailResponse;
import com.example.crm.userauth.module.entity.SysUser;
import com.example.crm.userauth.module.entity.UserPagePermission;
import com.example.crm.userauth.module.mapper.SysUserMapper;
import com.example.crm.userauth.module.mapper.UserPagePermissionMapper;
import com.example.crm.userauth.module.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final UserPagePermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;

    public IPage<SysUser> list(int page, int size, String keyword, Long orgId, String role) {
        // ORG_ADMIN 只能看自己组织的用户，除非有额外 orgId 参数
        if (UserContext.isOrgAdmin() && orgId == null) {
            orgId = UserContext.getOrgId();
        }
        return sysUserMapper.listPage(new Page<>(page, size), keyword, role, orgId);
    }

    public UserDetailResponse getById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || user.getIsDeleted()) {
            return null;
        }

        List<UserPagePermission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<UserPagePermission>()
                        .eq(UserPagePermission::getUserId, id)
                        .eq(UserPagePermission::getIsDeleted, false));

        UserDetailResponse response = new UserDetailResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRealName(user.getRealName());
        response.setOrgId(user.getOrgId());
        response.setOrgName(user.getOrgName());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setPermissions(permissions.stream()
                .map(p -> new UserDetailResponse.PagePermissionDTO(
                        p.getPageCode(), p.getCanView(), p.getCanEdit()))
                .collect(Collectors.toList()));
        return response;
    }

    public SysUser create(CreateUserRequest request) {
        // Check username uniqueness
        if (sysUserMapper.findUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setOrgId(request.getOrgId());
        user.setRole(request.getRole());
        user.setIsActive(true);
        sysUserMapper.insert(user);
        return user;
    }

    public void update(Long id, SysUser updates) {
        SysUser existing = sysUserMapper.selectById(id);
        if (existing == null || existing.getIsDeleted()) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
        if (updates.getPhone() != null) existing.setPhone(updates.getPhone());
        if (updates.getRealName() != null) existing.setRealName(updates.getRealName());
        if (updates.getOrgId() != null) existing.setOrgId(updates.getOrgId());
        if (updates.getRole() != null) existing.setRole(updates.getRole());
        if (updates.getIsActive() != null) existing.setIsActive(updates.getIsActive());
        sysUserMapper.updateById(existing);
    }

    public void delete(Long id) {
        permissionMapper.delete(new LambdaQueryWrapper<UserPagePermission>()
                .eq(UserPagePermission::getUserId, id)
                .eq(UserPagePermission::getIsDeleted, false));
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || user.getIsDeleted()) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setIsDeleted(true);
        user.setDeletedAt(java.time.OffsetDateTime.now());
        sysUserMapper.updateById(user);
    }

    public void resetPassword(Long id, String newPassword) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || user.getIsDeleted()) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
    }
}
