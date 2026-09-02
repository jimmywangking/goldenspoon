package com.example.crm.userauth;

import com.example.crm.userauth.module.dto.LoginRequest;
import com.example.crm.userauth.module.dto.LoginResponse;
import com.example.crm.userauth.module.entity.Org;
import com.example.crm.userauth.module.entity.SysUser;
import com.example.crm.userauth.module.mapper.OrgMapper;
import com.example.crm.userauth.module.mapper.SysUserMapper;
import com.example.crm.userauth.module.service.AuthService;
import com.example.crm.userauth.module.service.OrgService;
import com.example.crm.userauth.module.service.UserService;
import com.example.crm.userauth.module.service.PermissionService;
import com.example.crm.userauth.module.security.JwtUtils;
import com.example.crm.userauth.module.mapper.UserPagePermissionMapper;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthApplicationTests {

    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private OrgMapper orgMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PermissionService permissionService;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserPagePermissionMapper permissionMapper;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Reset state
    }

    @Test
    void login_success() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$12$hashed");
        user.setRole("USER");
        user.setIsActive(true);
        user.setRealName("测试用户");

        when(sysUserMapper.findByUsername("testuser")).thenReturn(user);
        when(passwordEncoder.matches("password123", "$2a$12$hashed")).thenReturn(true);
        when(permissionMapper.selectList(any())).thenReturn(List.of());
        when(jwtUtils.generateToken(anyLong(), anyString(), anyString(), any())).thenReturn("test-token");
        when(jwtUtils.generateRefreshToken(anyLong(), anyString())).thenReturn("test-refresh");
        when(jwtUtils.getExpirationMs()).thenReturn(86400000L);

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        LoginResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void login_userNotFound() {
        when(sysUserMapper.findByUsername("unknown")).thenReturn(null);

        assertThatThrownBy(() -> {
            LoginRequest request = new LoginRequest();
            request.setUsername("unknown");
            request.setPassword("password");
            authService.login(request);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void login_wrongPassword() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$12$hashed");
        user.setRole("USER");
        user.setIsActive(true);

        when(sysUserMapper.findByUsername("testuser")).thenReturn(user);
        when(passwordEncoder.matches("wrongpass", "$2a$12$hashed")).thenReturn(false);

        assertThatThrownBy(() -> {
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("wrongpass");
            authService.login(request);
        }).isInstanceOf(IllegalArgumentException.class);
    }
}
