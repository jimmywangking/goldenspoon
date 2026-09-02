package com.example.crm.userauth.module.service;

import com.example.crm.userauth.module.dto.PagePermissionRequest;
import com.example.crm.userauth.module.entity.UserPagePermission;
import com.example.crm.userauth.module.mapper.UserPagePermissionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private UserPagePermissionMapper permissionMapper;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void setPermissions_clearsOldAndInsertsNew() {
        PagePermissionRequest request = new PagePermissionRequest();
        request.setUserId(1L);

        PagePermissionRequest.PagePermissionItem item = new PagePermissionRequest.PagePermissionItem();
        item.setPageCode("PAGE_1");
        item.setCanView(true);
        item.setCanEdit(false);
        request.setPermissions(List.of(item));

        when(permissionMapper.delete(any())).thenReturn(1);
        when(permissionMapper.insert(any())).thenReturn(1);

        permissionService.setPermissions(1L, request);

        verify(permissionMapper).delete(any());
        verify(permissionMapper).insert(any(UserPagePermission.class));
    }

    @Test
    void getPermissions_returnsList() {
        UserPagePermission perm = new UserPagePermission();
        perm.setPageCode("PAGE_1");
        perm.setCanView(true);
        perm.setCanEdit(false);

        when(permissionMapper.selectList(any())).thenReturn(List.of(perm));

        List<UserPagePermission> result = permissionService.getPermissions(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPageCode()).isEqualTo("PAGE_1");
    }
}
