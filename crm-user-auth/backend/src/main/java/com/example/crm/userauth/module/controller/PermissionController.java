package com.example.crm.userauth.module.controller;

import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.dto.PagePermissionRequest;
import com.example.crm.userauth.module.entity.UserPagePermission;
import com.example.crm.userauth.module.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public Result<List<UserPagePermission>> getPermissions(@PathVariable Long userId) {
        return Result.ok(permissionService.getPermissions(userId));
    }

    @PutMapping
    public Result<Void> setPermissions(@PathVariable Long userId,
                                       @RequestBody PagePermissionRequest request) {
        permissionService.setPermissions(userId, request);
        return Result.ok();
    }
}
