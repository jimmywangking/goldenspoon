package com.example.crm.userauth.module.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.entity.Org;
import com.example.crm.userauth.module.security.UserContext;
import com.example.crm.userauth.module.service.OrgService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orgs")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @GetMapping
    public Result<Page<Org>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        Long orgId = UserContext.isOrgAdmin() ? UserContext.getOrgId() : null;
        return Result.ok(orgService.list(page, size, keyword, orgId));
    }

    @GetMapping("/{id}")
    public Result<Org> getById(@PathVariable Long id) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        Org org = orgService.getById(id);
        if (org == null) return Result.fail(404, "组织不存在");
        return Result.ok(org);
    }

    @PostMapping
    public Result<Org> create(@Valid @RequestBody Org org) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        return Result.ok(orgService.create(org));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Org org) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        org.setId(id);
        orgService.update(org);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        orgService.delete(id);
        return Result.ok();
    }
}
