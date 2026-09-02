package com.example.crm.userauth.module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.module.entity.Role;
import com.example.crm.userauth.module.entity.RolePagePermission;
import com.example.crm.userauth.module.mapper.RoleMapper;
import com.example.crm.userauth.module.mapper.RolePagePermissionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleServiceTest {

    @Mock private RoleMapper roleMapper;
    @Mock private RolePagePermissionMapper rolePagePermissionMapper;

    @InjectMocks
    private RoleService roleService;

    @Test
    void list_withKeyword() {
        Page<Role> page = new Page<>(1, 10);
        when(roleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<Role> result = roleService.list(1, 10, "管理员");

        assertThat(result).isNotNull();
        verify(roleMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void list_noKeyword() {
        Page<Role> page = new Page<>(1, 10);
        when(roleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        roleService.list(1, 10, null);

        verify(roleMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getById_found() {
        Role role = new Role();
        role.setId(1L);
        role.setName("测试角色");
        when(roleMapper.selectById(1L)).thenReturn(role);

        Role result = roleService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("测试角色");
    }

    @Test
    void getById_notFound() {
        when(roleMapper.selectById(999L)).thenReturn(null);
        assertThat(roleService.getById(999L)).isNull();
    }

    @Test
    void create_setsIsSystemDefault() {
        Role role = new Role();
        role.setName("新角色");
        role.setCode("NEW_ROLE");
        when(roleMapper.insert(any(Role.class))).thenReturn(1);

        Role result = roleService.create(role);

        assertThat(result.getIsSystem()).isFalse();
        verify(roleMapper).insert(role);
    }

    @Test
    void create_defaultIsSystem_null() {
        Role role = new Role();
        role.setName("新角色");
        role.setCode("TEST");
        when(roleMapper.insert(any(Role.class))).thenReturn(1);

        roleService.create(role);

        assertThat(role.getIsSystem()).isFalse();
    }

    @Test
    void update_success() {
        Role existing = new Role();
        existing.setId(1L);
        existing.setIsSystem(false);
        when(roleMapper.selectById(1L)).thenReturn(existing);
        when(roleMapper.updateById(any(Role.class))).thenReturn(1);

        Role update = new Role();
        update.setId(1L);
        update.setName("新名称");

        roleService.update(update);

        verify(roleMapper).updateById(update);
    }

    @Test
    void update_notFound_throws() {
        when(roleMapper.selectById(999L)).thenReturn(null);
        Role r = new Role();
        r.setId(999L);
        assertThatThrownBy(() -> roleService.update(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("角色不存在");
    }

    @Test
    void update_systemRole_throws() {
        Role existing = new Role();
        existing.setId(1L);
        existing.setIsSystem(true);
        when(roleMapper.selectById(1L)).thenReturn(existing);

        Role r = new Role();
        r.setId(1L);
        assertThatThrownBy(() -> roleService.update(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("系统角色不可修改");
    }

    @Test
    void delete_success() {
        Role role = new Role();
        role.setId(1L);
        role.setIsSystem(false);
        when(roleMapper.selectById(1L)).thenReturn(role);
        when(roleMapper.deleteById(1L)).thenReturn(1);

        roleService.delete(1L);

        verify(roleMapper).deleteById(1L);
    }

    @Test
    void delete_notFound_throws() {
        when(roleMapper.selectById(999L)).thenReturn(null);
        assertThatThrownBy(() -> roleService.delete(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("角色不存在");
    }

    @Test
    void delete_systemRole_throws() {
        Role role = new Role();
        role.setId(1L);
        role.setIsSystem(true);
        when(roleMapper.selectById(1L)).thenReturn(role);

        assertThatThrownBy(() -> roleService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("系统角色不可删除");
    }

    @Test
    void getPermissions_found() {
        List<RolePagePermission> perms = List.of(new RolePagePermission());
        when(rolePagePermissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(perms);

        List<RolePagePermission> result = roleService.getPermissions(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void setPermissions_clearsOldAndInsertsNew() {
        List<RolePagePermission> newPerms = List.of(
                new RolePagePermission(),
                new RolePagePermission()
        );
        when(rolePagePermissionMapper.insert(any())).thenReturn(1);

        roleService.setPermissions(1L, newPerms);

        verify(rolePagePermissionMapper).delete(any(LambdaQueryWrapper.class));
        verify(rolePagePermissionMapper, times(2)).insert(any());
    }
}
