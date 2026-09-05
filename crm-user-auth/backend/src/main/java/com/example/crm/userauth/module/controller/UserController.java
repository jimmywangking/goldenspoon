package com.example.crm.userauth.module.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.dto.CreateUserRequest;
import com.example.crm.userauth.module.dto.UserDetailResponse;
import com.example.crm.userauth.module.entity.SysUser;
import com.example.crm.userauth.module.security.UserContext;
import com.example.crm.userauth.module.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户CRUD操作，需要ADMIN或ORG_ADMIN权限")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "分页查询用户列表",
        description = "支持按关键字、角色、组织ID筛选。ADMIN查看全部，ORG_ADMIN只看本组织",
        responses = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无权限访问")
        }
    )
    @GetMapping
    public Result<IPage<SysUser>> list(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "搜索关键词（用户名/姓名）") @RequestParam(required = false) String keyword,
            @Parameter(description = "组织ID过滤") @RequestParam(required = false) Long orgId,
            @Parameter(description = "角色过滤（ADMIN/ORG_ADMIN/USER）") @RequestParam(required = false) String role) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        return Result.ok(userService.list(page, size, keyword, orgId, role));
    }

    @Operation(summary = "获取用户详情", description = "根据ID查询用户详细信息")
    @GetMapping("/{id}")
    public Result<UserDetailResponse> getById(@PathVariable Long id) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        UserDetailResponse user = userService.getById(id);
        if (user == null) return Result.fail(404, "用户不存在");
        return Result.ok(user);
    }

    @Operation(
        summary = "创建用户",
        description = "创建新用户。ORG_ADMIN只能创建到自己组织的用户",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "用户创建请求体",
            required = true
        )
    )
    @PostMapping
    public Result<SysUser> create(@Valid @RequestBody CreateUserRequest request) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        if (UserContext.isOrgAdmin()) {
            request.setOrgId(UserContext.getOrgId());
        }
        return Result.ok(userService.create(request));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser updates) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        userService.update(id, updates);
        return Result.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        userService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "重置用户密码")
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            return Result.fail(400, "密码不能为空");
        }
        userService.resetPassword(id, newPassword);
        return Result.ok();
    }
}
