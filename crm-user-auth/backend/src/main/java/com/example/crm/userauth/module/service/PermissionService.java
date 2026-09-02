package com.example.crm.userauth.module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.crm.userauth.module.dto.PagePermissionRequest;
import com.example.crm.userauth.module.entity.UserPagePermission;
import com.example.crm.userauth.module.mapper.UserPagePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final UserPagePermissionMapper permissionMapper;

    public List<UserPagePermission> getPermissions(Long userId) {
        return permissionMapper.selectList(new LambdaQueryWrapper<UserPagePermission>()
                .eq(UserPagePermission::getUserId, userId)
                .eq(UserPagePermission::getIsDeleted, false));
    }

    public void setPermissions(Long userId, PagePermissionRequest request) {
        // Remove existing permissions for this user
        permissionMapper.delete(new LambdaQueryWrapper<UserPagePermission>()
                .eq(UserPagePermission::getUserId, userId)
                .eq(UserPagePermission::getIsDeleted, false));

        // Insert new permissions
        if (request.getPermissions() != null) {
            for (PagePermissionRequest.PagePermissionItem item : request.getPermissions()) {
                UserPagePermission perm = new UserPagePermission();
                perm.setUserId(userId);
                perm.setPageCode(item.getPageCode());
                perm.setCanView(item.getCanView() != null ? item.getCanView() : true);
                perm.setCanEdit(item.getCanEdit() != null ? item.getCanEdit() : false);
                permissionMapper.insert(perm);
            }
        }
    }
}
