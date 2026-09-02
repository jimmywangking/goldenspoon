package com.example.crm.userauth.module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.module.dto.CreateUserRequest;
import com.example.crm.userauth.module.dto.UserDetailResponse;
import com.example.crm.userauth.module.entity.SysUser;
import com.example.crm.userauth.module.entity.UserPagePermission;
import com.example.crm.userauth.module.mapper.SysUserMapper;
import com.example.crm.userauth.module.mapper.UserPagePermissionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private UserPagePermissionMapper permissionMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void create_user_success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRealName("新用户");
        request.setRole("USER");
        request.setOrgId(1L);

        when(sysUserMapper.findUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$hashed");
        when(sysUserMapper.insert(any(SysUser.class))).thenReturn(1);

        SysUser result = userService.create(request);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.getOrgId()).isEqualTo(1L);
        verify(sysUserMapper).insert(any(SysUser.class));
    }

    @Test
    void create_user_duplicateUsername_throws() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("existing");
        request.setPassword("pass");
        request.setRole("USER");

        when(sysUserMapper.findUsername("existing")).thenReturn("existing");

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    void resetPassword_success() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setIsDeleted(false);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.encode("newpass")).thenReturn("$2a$12$newhashed");

        userService.resetPassword(1L, "newpass");

        verify(sysUserMapper).updateById(user);
        assertThat(user.getPassword()).isEqualTo("$2a$12$newhashed");
    }

    @Test
    void delete_user_success() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setIsDeleted(false);
        when(permissionMapper.delete(any())).thenReturn(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);

        userService.delete(1L);

        verify(sysUserMapper).updateById(user);
        assertThat(user.getIsDeleted()).isTrue();
    }

    @Test
    void delete_user_notFound_throws() {
        when(permissionMapper.delete(any())).thenReturn(1);
        when(sysUserMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户不存在");
    }
}
