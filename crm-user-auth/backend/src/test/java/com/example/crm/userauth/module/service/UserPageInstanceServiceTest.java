package com.example.crm.userauth.module.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.module.entity.SysUser;
import com.example.crm.userauth.module.entity.UserPageInstance;
import com.example.crm.userauth.module.mapper.SysUserMapper;
import com.example.crm.userauth.module.mapper.UserPageInstanceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPageInstanceServiceTest {

    @Mock private UserPageInstanceMapper instanceMapper;
    @Mock private SysUserMapper sysUserMapper;

    @InjectMocks
    private UserPageInstanceService service;

    // ── list ──────────────────────────────────────────────────────

    @Test
    void listByUser_success() {
        when(instanceMapper.listByUser(1L, "PAGE_1", 1, 10))
                .thenReturn(List.of(makeInstance(1L, 1L, "PAGE_1", "内容A")));
        when(instanceMapper.countByUserAndPageCode(1L, "PAGE_1")).thenReturn(1L);

        Page<UserPageInstance> result = service.listByUser(1L, "PAGE_1", 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void listByUser_empty() {
        when(instanceMapper.listByUser(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(List.of());
        when(instanceMapper.countByUserAndPageCode(anyLong(), anyString())).thenReturn(0L);

        Page<UserPageInstance> result = service.listByUser(1L, "PAGE_2", 1, 10);
        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getTotal()).isZero();
    }

    // ── admin list ────────────────────────────────────────────────

    @Test
    void listAll_adminSuccess() {
        when(instanceMapper.countAllByPage("PAGE_1", null)).thenReturn(3L);
        when(instanceMapper.listAllByPage(eq("PAGE_1"), isNull(), eq(1), eq(10)))
                .thenReturn(List.of(makeInstance(1L, 1L, "PAGE_1", "")));

        Page<UserPageInstance> result = service.listAll("PAGE_1", null, true, 1, 10);
        assertThat(result.getTotal()).isEqualTo(3);
    }

    @Test
    void listAll_nonAdmin_throws() {
        assertThatThrownBy(() -> service.listAll("PAGE_1", 1L, false, 1, 10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("仅管理员可访问");
    }

    // ── getById ───────────────────────────────────────────────────

    @Test
    void getById_notFound_throws() {
        when(instanceMapper.selectById(999L)).thenReturn(null);
        assertThatThrownBy(() -> service.getById(999L, 1L, "USER", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("实例不存在");
    }

    @Test
    void getById_deleted_throws() {
        UserPageInstance inst = makeInstance(1L, 1L, "PAGE_1", "x");
        inst.setIsDeleted(true);
        when(instanceMapper.selectById(1L)).thenReturn(inst);
        assertThatThrownBy(() -> service.getById(1L, 1L, "USER", 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getById_adminCanViewAny() {
        when(instanceMapper.selectById(2L)).thenReturn(makeInstance(2L, 99L, "PAGE_1", "x"));
        UserPageInstance result = service.getById(2L, 1L, "ADMIN", null);
        assertThat(result.getUserId()).isEqualTo(99L);
    }

    @Test
    void getById_ownerCanView() {
        UserPageInstance inst = makeInstance(1L, 1L, "PAGE_1", "x");
        when(instanceMapper.selectById(1L)).thenReturn(inst);
        UserPageInstance result = service.getById(1L, 1L, "USER", 1L);
        assertThat(result).isNotNull();
    }

    @Test
    void getById_otherUserCannotView() {
        UserPageInstance inst = makeInstance(1L, 2L, "PAGE_1", "x"); // user_id=2
        when(instanceMapper.selectById(1L)).thenReturn(inst);
        assertThatThrownBy(() -> service.getById(1L, 1L, "USER", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("无权访问");
    }

    @Test
    void getById_orgAdmin_sameOrg_canView() {
        SysUser owner = new SysUser();
        owner.setId(2L);
        owner.setOrgId(3L);
        when(sysUserMapper.selectById(2L)).thenReturn(owner);
        when(instanceMapper.selectById(1L)).thenReturn(makeInstance(1L, 2L, "PAGE_1", "x"));
        UserPageInstance result = service.getById(1L, 1L, "ORG_ADMIN", 3L);
        assertThat(result).isNotNull();
    }

    @Test
    void getById_orgAdmin_differentOrg_cannotView() {
        SysUser owner = new SysUser();
        owner.setId(2L);
        owner.setOrgId(5L); // different org
        when(sysUserMapper.selectById(2L)).thenReturn(owner);
        when(instanceMapper.selectById(1L)).thenReturn(makeInstance(1L, 2L, "PAGE_1", "x"));
        assertThatThrownBy(() -> service.getById(1L, 1L, "ORG_ADMIN", 3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("无权访问");
    }

    // ── create ────────────────────────────────────────────────────

    @Test
    void create_user_success() {
        UserPageInstance inst = new UserPageInstance();
        inst.setUserId(1L);
        inst.setPageCode("PAGE_1");
        inst.setContent("{\"key\":\"val\"}");
        when(instanceMapper.countByUserAndPageCode(1L, "PAGE_1")).thenReturn(2L);
        when(instanceMapper.insert(any())).thenReturn(1);

        UserPageInstance result = service.create(inst, 1L, "USER", 1L);

        assertThat(result).isNotNull();
        verify(instanceMapper).insert(inst);
    }

    @Test
    void create_userExceedLimit_throws() {
        UserPageInstance inst = new UserPageInstance();
        inst.setUserId(1L);
        inst.setPageCode("PAGE_1");
        inst.setContent("{}");
        when(instanceMapper.countByUserAndPageCode(1L, "PAGE_1")).thenReturn(5L);

        assertThatThrownBy(() -> service.create(inst, 1L, "USER", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("超出页面实例数量限制");
    }

    @Test
    void create_orgAdmin_success() {
        UserPageInstance inst = new UserPageInstance();
        inst.setUserId(2L);
        inst.setPageCode("PAGE_2");
        inst.setContent("{}");
        when(instanceMapper.countByUserAndPageCode(2L, "PAGE_2")).thenReturn(1L);
        when(instanceMapper.insert(any())).thenReturn(1);

        UserPageInstance result = service.create(inst, 2L, "ORG_ADMIN", 3L);
        assertThat(result).isNotNull();
    }

    @Test
    void create_admin_noLimit() {
        UserPageInstance inst = new UserPageInstance();
        inst.setUserId(1L);
        inst.setPageCode("PAGE_1");
        inst.setContent("{}");
        when(instanceMapper.countByUserAndPageCode(1L, "PAGE_1")).thenReturn(999L);
        when(instanceMapper.insert(any())).thenReturn(1);

        service.create(inst, 1L, "ADMIN", null);
        verify(instanceMapper).insert(inst);
    }

    // ── update ────────────────────────────────────────────────────

    @Test
    void update_success() {
        UserPageInstance inst = makeInstance(1L, 1L, "PAGE_1", "old");
        when(instanceMapper.selectById(1L)).thenReturn(inst);
        when(instanceMapper.updateById(any())).thenReturn(1);

        service.update(1L, "新标题", "{\"a\":1}", 1L, "USER", 1L);

        assertThat(inst.getTitle()).isEqualTo("新标题");
        assertThat(inst.getContent()).isEqualTo("{\"a\":1}");
        verify(instanceMapper).updateById(inst);
    }

    @Test
    void update_notFound_throws() {
        when(instanceMapper.selectById(999L)).thenReturn(null);
        assertThatThrownBy(() -> service.update(999L, "t", "c", 1L, "USER", 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── softDelete ────────────────────────────────────────────────

    @Test
    void softDelete_success() {
        UserPageInstance inst = makeInstance(1L, 1L, "PAGE_1", "x");
        when(instanceMapper.selectById(1L)).thenReturn(inst);
        when(instanceMapper.updateById(any())).thenReturn(1);

        service.softDelete(1L, 1L, "USER", 1L);

        assertThat(inst.getIsDeleted()).isTrue();
        assertThat(inst.getDeletedAt()).isNotNull();
        verify(instanceMapper).updateById(inst);
    }

    @Test
    void softDelete_notFound_throws() {
        when(instanceMapper.selectById(999L)).thenReturn(null);
        assertThatThrownBy(() -> service.softDelete(999L, 1L, "USER", 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void softDelete_permissionDenied_throws() {
        UserPageInstance inst = makeInstance(1L, 2L, "PAGE_1", "x");
        when(instanceMapper.selectById(1L)).thenReturn(inst);
        assertThatThrownBy(() -> service.softDelete(1L, 1L, "USER", 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── helpers ───────────────────────────────────────────────────

    private UserPageInstance makeInstance(Long id, Long userId, String pageCode, String content) {
        UserPageInstance i = new UserPageInstance();
        i.setId(id);
        i.setUserId(userId);
        i.setPageCode(pageCode);
        i.setContent(content);
        i.setSortOrder(0);
        i.setIsDeleted(false);
        i.setCreatedAt(OffsetDateTime.now());
        i.setUpdatedAt(OffsetDateTime.now());
        return i;
    }
}
