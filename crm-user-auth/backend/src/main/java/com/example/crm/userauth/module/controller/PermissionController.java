package com.example.crm.userauth.module.controller;

import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.dto.PagePermissionRequest;
import com.example.crm.userauth.module.entity.UserPagePermission;
import com.example.crm.userauth.module.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "页面权限", description = "用户的页面访问权限配置")
@RestController
@RequestMapping("/api/users/{userId}/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "获取用户的页面权限列表")
    @GetMapping
    public Result<List<UserPagePermission>> getPermissions(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        return Result.ok(permissionService.getPermissions(userId));
    }

    @Operation(
        summary = "设置用户的页面权限",
        description = "body: [{pageCode: 'PAGE_1', canView: true, canEdit: true}, ...]",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "权限配置请求",
            required = true
        )
    )
    @PutMapping
    public Result<Void> setPermissions(
            @PathVariable Long userId,
            @RequestBody PagePermissionRequest request) {
        permissionService.setPermissions(userId, request);
        return Result.ok();
    }
}
