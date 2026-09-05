package com.example.crm.userauth.module.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.entity.Org;
import com.example.crm.userauth.module.security.UserContext;
import com.example.crm.userauth.module.service.OrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "组织管理", description = "组织CRUD操作，需要ADMIN或ORG_ADMIN权限")
@RestController
@RequestMapping("/api/orgs")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @Operation(
        summary = "分页查询组织列表",
        description = "ADMIN查看全部，ORG_ADMIN只看本组织",
        responses = @ApiResponse(responseCode = "200", description = "查询成功")
    )
    @GetMapping
    public Result<Page<Org>> list(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        Long orgId = UserContext.isOrgAdmin() ? UserContext.getOrgId() : null;
        return Result.ok(orgService.list(page, size, keyword, orgId));
    }

    @Operation(summary = "获取组织详情")
    @GetMapping("/{id}")
    public Result<Org> getById(@PathVariable Long id) {
        if (!UserContext.isAdmin() && !UserContext.isOrgAdmin()) {
            return Result.fail(403, "无权访问");
        }
        Org org = orgService.getById(id);
        if (org == null) return Result.fail(404, "组织不存在");
        return Result.ok(org);
    }

    @Operation(summary = "创建组织", description = "仅ADMIN可创建")
    @PostMapping
    public Result<Org> create(@Valid @RequestBody Org org) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        return Result.ok(orgService.create(org));
    }

    @Operation(summary = "更新组织信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Org org) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        org.setId(id);
        orgService.update(org);
        return Result.ok();
    }

    @Operation(summary = "删除组织")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        orgService.delete(id);
        return Result.ok();
    }
}
