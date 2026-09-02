package com.example.crm.userauth.module.service;

import com.example.crm.userauth.module.dto.LoginRequest;
import com.example.crm.userauth.module.dto.LoginResponse;
import com.example.crm.userauth.module.entity.SysUser;
import com.example.crm.userauth.module.entity.UserPagePermission;
import com.example.crm.userauth.module.mapper.SysUserMapper;
import com.example.crm.userauth.module.mapper.UserPagePermissionMapper;
import com.example.crm.userauth.module.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private SysUserMapper sysUserMapper;
    @Mock private UserPagePermissionMapper permissionMapper;
    @Mock private JwtUtils jwtUtils;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // ── login ─────────────────────────────────────────────────────

    @Test
    void login_adminSuccess() {
        SysUser user = buildUser(1L, "admin", "ADMIN", null, null);
        when(sysUserMapper.findByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("pass", "$2a$12$hash")).thenReturn(true);
        when(jwtUtils.generateToken(1L, "admin", "ADMIN", null)).thenReturn("jwt");
        when(jwtUtils.generateRefreshToken(1L, "admin")).thenReturn("refresh");
        when(jwtUtils.getExpirationMs()).thenReturn(86400000L);

        LoginResponse response = authService.login(buildLogin("admin", "pass"));

        assertThat(response.getToken()).isEqualTo("jwt");
        assertThat(response.getUser().isAdmin()).isTrue();
        assertThat(response.getUser().getPermissions()).hasSize(2);
        assertThat(response.getUser().getPermissions()[0].getPageCode()).isEqualTo("PAGE_1");
    }

    @Test
    void login_userWithPermissions() {
        SysUser user = buildUser(2L, "user1", "USER", 3L, "测试组织");
        when(sysUserMapper.findByUsername("user1")).thenReturn(user);
        when(passwordEncoder.matches("pass", "$2a$12$hash")).thenReturn(true);
        when(permissionMapper.selectList(any())).thenReturn(List.of(buildPerm("PAGE_1", true, true)));
        when(jwtUtils.generateToken(2L, "user1", "USER", 3L)).thenReturn("jwt");
        when(jwtUtils.generateRefreshToken(2L, "user1")).thenReturn("refresh");
        when(jwtUtils.getExpirationMs()).thenReturn(86400000L);

        LoginResponse response = authService.login(buildLogin("user1", "pass"));

        assertThat(response.getUser().getUsername()).isEqualTo("user1");
        assertThat(response.getUser().getOrgName()).isEqualTo("测试组织");
        assertThat(response.getUser().getPermissions()).hasSize(1);
        assertThat(response.getUser().getPermissions()[0].getPageCode()).isEqualTo("PAGE_1");
    }

    @Test
    void login_userNotFound_throws() {
        when(sysUserMapper.findByUsername("ghost")).thenReturn(null);
        assertThatThrownBy(() -> authService.login(buildLogin("ghost", "x")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void login_wrongPassword_throws() {
        SysUser user = buildUser(1L, "admin", "ADMIN", null, null);
        when(sysUserMapper.findByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "$2a$12$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(buildLogin("admin", "wrong")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void login_inactiveUser_throws() {
        SysUser user = buildUser(1L, "admin", "ADMIN", null, null);
        user.setIsActive(false);
        when(sysUserMapper.findByUsername("admin")).thenReturn(user);

        assertThatThrownBy(() -> authService.login(buildLogin("admin", "pass")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── getCurrentUser ────────────────────────────────────────────

    @Test
    void getCurrentUser_found() {
        SysUser user = buildUser(1L, "admin", "ADMIN", null, null);
        when(sysUserMapper.findByUsername("admin")).thenReturn(user);

        SysUser result = authService.getCurrentUser("admin");

        assertThat(result.getUsername()).isEqualTo("admin");
    }

    @Test
    void getCurrentUser_notFound() {
        when(sysUserMapper.findByUsername("ghost")).thenReturn(null);
        assertThat(authService.getCurrentUser("ghost")).isNull();
    }

    // ── buildUserInfo ─────────────────────────────────────────────

    @Test
    void buildUserInfo_admin() {
        SysUser user = buildUser(1L, "admin", "ADMIN", null, null);
        LoginResponse.UserInfo info = authService.buildUserInfo(user);

        assertThat(info.isAdmin()).isTrue();
        assertThat(info.getPermissions()).hasSize(2);
    }

    @Test
    void buildUserInfo_userWithNoPermissions() {
        SysUser user = buildUser(2L, "user1", "USER", 3L, "组织A");
        LoginResponse.UserInfo info = authService.buildUserInfo(user);

        assertThat(info.isAdmin()).isFalse();
        assertThat(info.getPermissions()).isEmpty();
    }

    @Test
    void buildUserInfo_orgAdmin() {
        SysUser user = buildUser(3L, "orgadmin", "ORG_ADMIN", 3L, "组织A");
        when(permissionMapper.selectList(any())).thenReturn(List.of(
                buildPerm("PAGE_1", true, true),
                buildPerm("PAGE_2", true, false)
        ));
        LoginResponse.UserInfo info = authService.buildUserInfo(user);

        assertThat(info.isAdmin()).isFalse();
        assertThat(info.getPermissions()).hasSize(2);
        assertThat(info.getPermissions()[0].isCanEdit()).isTrue();
        assertThat(info.getPermissions()[1].isCanEdit()).isFalse();
    }

    // ── helpers ───────────────────────────────────────────────────

    private SysUser buildUser(Long id, String username, String role, Long orgId, String orgName) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("$2a$12$hash");
        user.setRole(role);
        user.setIsActive(true);
        user.setRealName(username + "名");
        user.setOrgId(orgId);
        user.setOrgName(orgName);
        return user;
    }

    private UserPagePermission buildPerm(String pageCode, boolean canView, boolean canEdit) {
        UserPagePermission p = new UserPagePermission();
        p.setPageCode(pageCode);
        p.setCanView(canView);
        p.setCanEdit(canEdit);
        return p;
    }

    private LoginRequest buildLogin(String username, String password) {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        return req;
    }
}
