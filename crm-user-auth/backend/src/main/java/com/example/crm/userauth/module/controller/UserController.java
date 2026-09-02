package com.example.crm.userauth.module.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.dto.CreateUserRequest;
import com.example.crm.userauth.module.dto.UserDetailResponse;
import com.example.crm.userauth.module.entity.SysUser;
import com.example.crm.userauth.module.security.UserContext;
import com.example.crm.userauth.module.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<IPage<SysUser>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String role) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        return Result.ok(userService.list(page, size, keyword, orgId, role));
    }

    @GetMapping("/{id}")
    public Result<UserDetailResponse> getById(@PathVariable Long id) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        UserDetailResponse user = userService.getById(id);
        if (user == null) return Result.fail(404, "用户不存在");
        return Result.ok(user);
    }

    @PostMapping
    public Result<SysUser> create(@Valid @RequestBody CreateUserRequest request) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        // ORG_ADMIN 只能创建到自己组织的用户
        if (UserContext.isOrgAdmin()) {
            request.setOrgId(UserContext.getOrgId());
        }
        return Result.ok(userService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser updates) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        userService.update(id, updates);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        userService.delete(id);
        return Result.ok();
    }

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
