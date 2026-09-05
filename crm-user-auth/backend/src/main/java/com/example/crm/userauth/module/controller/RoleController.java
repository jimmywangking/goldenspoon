package com.example.crm.userauth.module.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.entity.Role;
import com.example.crm.userauth.module.entity.RolePagePermission;
import com.example.crm.userauth.module.security.UserContext;
import com.example.crm.userauth.module.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理", description = "角色CRUD及页面权限配置，仅ADMIN可访问")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "分页查询角色列表")
    @GetMapping
    public Result<Page<Role>> list(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        return Result.ok(roleService.list(page, size, keyword));
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        Role role = roleService.getById(id);
        if (role == null) return Result.fail(404, "角色不存在");
        return Result.ok(role);
    }

    @Operation(summary = "创建角色", description = "isSystem默认false，不可为null")
    @PostMapping
    public Result<Role> create(@Valid @RequestBody Role role) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        return Result.ok(roleService.create(role));
    }

    @Operation(summary = "更新角色信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Role role) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        role.setId(id);
        roleService.update(role);
        return Result.ok();
    }

    @Operation(summary = "删除角色", description = "系统角色不可删除")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        roleService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "获取角色的页面权限")
    @GetMapping("/{id}/permissions")
    public Result<List<RolePagePermission>> getPermissions(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        return Result.ok(roleService.getPermissions(id));
    }

    @Operation(summary = "设置角色的页面权限",
        description = "清除旧权限，插入新权限列表")
    @PutMapping("/{id}/permissions")
    public Result<Void> setPermissions(@PathVariable Long id, @RequestBody List<RolePagePermission> permissions) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        roleService.setPermissions(id, permissions);
        return Result.ok();
    }
}
