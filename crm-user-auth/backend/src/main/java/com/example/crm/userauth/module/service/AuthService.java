package com.example.crm.userauth.module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.module.dto.LoginRequest;
import com.example.crm.userauth.module.dto.LoginResponse;
import com.example.crm.userauth.module.dto.RegisterRequest;
import com.example.crm.userauth.module.dto.RegistrationDTO;
import com.example.crm.userauth.module.entity.SysUser;
import com.example.crm.userauth.module.entity.UserPagePermission;
import com.example.crm.userauth.module.mapper.SysUserMapper;
import com.example.crm.userauth.module.mapper.UserPagePermissionMapper;
import com.example.crm.userauth.module.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final UserPagePermissionMapper permissionMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.findByUsername(request.getUsername());
        if (user == null || !user.getIsActive()) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!"APPROVED".equals(user.getStatus())) {
            throw new IllegalArgumentException("账户待审批，请等待管理员通过");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getOrgId());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        List<UserPagePermission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<UserPagePermission>()
                        .eq(UserPagePermission::getUserId, user.getId())
                        .eq(UserPagePermission::getIsDeleted, false));

        LoginResponse.PagePermission[] pagePerms = user.getRole().equals("ADMIN")
                ? new LoginResponse.PagePermission[]{
                    new LoginResponse.PagePermission("PAGE_1", true, true),
                    new LoginResponse.PagePermission("PAGE_2", true, true)
                }
                : permissions.stream()
                        .map(p -> new LoginResponse.PagePermission(
                                p.getPageCode(), p.getCanView(), p.getCanEdit()))
                        .toArray(LoginResponse.PagePermission[]::new);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(), user.getUsername(), user.getRealName(),
                user.getRole(), user.getOrgId(), user.getOrgName(),
                "ADMIN".equals(user.getRole()), pagePerms);

        return new LoginResponse(token, refreshToken, jwtUtils.getExpirationMs() / 1000, userInfo);
    }

    public SysUser getCurrentUser(String username) {
        return sysUserMapper.findByUsername(username);
    }

    public LoginResponse.UserInfo buildUserInfo(SysUser user) {
        List<UserPagePermission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<UserPagePermission>()
                        .eq(UserPagePermission::getUserId, user.getId())
                        .eq(UserPagePermission::getIsDeleted, false));

        LoginResponse.PagePermission[] pagePerms = user.getRole().equals("ADMIN")
                ? new LoginResponse.PagePermission[]{
                    new LoginResponse.PagePermission("PAGE_1", true, true),
                    new LoginResponse.PagePermission("PAGE_2", true, true)}
                : permissions.stream()
                        .map(p -> new LoginResponse.PagePermission(
                                p.getPageCode(), p.getCanView(), p.getCanEdit()))
                        .toArray(LoginResponse.PagePermission[]::new);

        return new LoginResponse.UserInfo(
                user.getId(), user.getUsername(), user.getRealName(),
                user.getRole(), user.getOrgId(), user.getOrgName(),
                "ADMIN".equals(user.getRole()), pagePerms);
    }

    public SysUser register(RegisterRequest request) {
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
        user.setRole("USER");
        user.setIsActive(true);
        user.setStatus("PENDING");
        sysUserMapper.insert(user);
        return user;
    }

    public Page<RegistrationDTO> getPendingRegistrations(int page, int size) {
        Page<SysUser> p = sysUserMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStatus, "PENDING")
                        .eq(SysUser::getIsDeleted, false)
                        .orderByDesc(SysUser::getCreatedAt));
        Page<RegistrationDTO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(p.getRecords().stream()
                .map(RegistrationDTO::fromEntity)
                .collect(Collectors.toList()));
        return result;
    }

    public void approve(Long id, boolean approved) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || user.getIsDeleted()) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!"PENDING".equals(user.getStatus())) {
            throw new IllegalArgumentException("该用户不是待审批状态");
        }
        user.setStatus(approved ? "APPROVED" : "REJECTED");
        sysUserMapper.updateById(user);
    }
}
