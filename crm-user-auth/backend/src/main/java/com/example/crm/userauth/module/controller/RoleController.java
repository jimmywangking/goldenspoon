package com.example.crm.userauth.module.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.entity.Role;
import com.example.crm.userauth.module.entity.RolePagePermission;
import com.example.crm.userauth.module.security.UserContext;
import com.example.crm.userauth.module.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public Result<Page<Role>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        return Result.ok(roleService.list(page, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        Role role = roleService.getById(id);
        if (role == null) return Result.fail(404, "角色不存在");
        return Result.ok(role);
    }

    @PostMapping
    public Result<Role> create(@Valid @RequestBody Role role) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        return Result.ok(roleService.create(role));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Role role) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        role.setId(id);
        roleService.update(role);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        roleService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}/permissions")
    public Result<List<RolePagePermission>> getPermissions(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        return Result.ok(roleService.getPermissions(id));
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> setPermissions(@PathVariable Long id, @RequestBody List<RolePagePermission> permissions) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        roleService.setPermissions(id, permissions);
        return Result.ok();
    }
}
