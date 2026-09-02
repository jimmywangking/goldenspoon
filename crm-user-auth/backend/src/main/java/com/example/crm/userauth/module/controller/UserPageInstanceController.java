package com.example.crm.userauth.module.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.entity.UserPageInstance;
import com.example.crm.userauth.module.security.UserContext;
import com.example.crm.userauth.module.service.UserPageInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/instances")
@RequiredArgsConstructor
public class UserPageInstanceController {

    private final UserPageInstanceService instanceService;

    @GetMapping
    public Result<Page<UserPageInstance>> list(
            @RequestParam(required = false) String pageCode,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        Page<UserPageInstance> result = instanceService.listByUser(userId, pageCode, current, size);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<UserPageInstance> getById(@PathVariable Long id) {
        UserPageInstance instance = instanceService.getById(
                id, UserContext.getUserId(), UserContext.getRole(), UserContext.getOrgId());
        return Result.ok(instance);
    }

    @PostMapping
    public Result<UserPageInstance> create(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        Long orgId = UserContext.getOrgId();

        UserPageInstance instance = new UserPageInstance();
        instance.setUserId(userId);
        instance.setPageCode((String) body.get("pageCode"));
        instance.setTitle(body.get("title") != null ? body.get("title").toString() : null);
        instance.setContent((String) body.get("content"));
        instance.setCreatedBy(userId);

        instanceService.create(instance, userId, role, orgId);
        return Result.ok(instance);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        instanceService.update(id,
                body.get("title"), body.get("content"),
                UserContext.getUserId(), UserContext.getRole(), UserContext.getOrgId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        instanceService.softDelete(id,
                UserContext.getUserId(), UserContext.getRole(), UserContext.getOrgId());
        return Result.ok();
    }

    @GetMapping("/admin/all")
    public Result<Page<UserPageInstance>> adminList(
            @RequestParam(required = false) String pageCode,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        Page<UserPageInstance> result = instanceService.listAll(pageCode, UserContext.getOrgId(), true, current, size);
        return Result.ok(result);
    }
}
