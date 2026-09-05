package com.example.crm.userauth.module.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.entity.UserPageInstance;
import com.example.crm.userauth.module.security.UserContext;
import com.example.crm.userauth.module.service.UserPageInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "页面实例", description = "用户页面实例CRUD，按角色分层可见，USER最多5个/每页类型")
@RestController
@RequestMapping("/api/instances")
@RequiredArgsConstructor
public class UserPageInstanceController {

    private final UserPageInstanceService instanceService;

    @Operation(
        summary = "分页查询我的实例",
        description = "USER角色每个pageCode最多5个，ORG_ADMIN最多20个，ADMIN无限制"
    )
    @GetMapping
    public Result<Page<UserPageInstance>> list(
            @Parameter(description = "页面类型过滤（PAGE_1/PAGE_2）") @RequestParam(required = false) String pageCode,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量，默认10") @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        Page<UserPageInstance> result = instanceService.listByUser(userId, pageCode, current, size);
        return Result.ok(result);
    }

    @Operation(summary = "获取实例详情", description = "包含权限校验：USER只能看自己的，ORG_ADMIN看同组织")
    @GetMapping("/{id}")
    public Result<UserPageInstance> getById(@PathVariable Long id) {
        UserPageInstance instance = instanceService.getById(
                id, UserContext.getUserId(), UserContext.getRole(), UserContext.getOrgId());
        return Result.ok(instance);
    }

    @Operation(
        summary = "创建实例",
        description = "body: {pageCode: 'PAGE_1', title: '可选', content: 'JSON字符串'}",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "页面实例创建请求",
            required = true
        )
    )
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

    @Operation(summary = "更新实例标题或内容")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        instanceService.update(id,
                body.get("title"), body.get("content"),
                UserContext.getUserId(), UserContext.getRole(), UserContext.getOrgId());
        return Result.ok();
    }

    @Operation(summary = "软删除实例")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        instanceService.softDelete(id,
                UserContext.getUserId(), UserContext.getRole(), UserContext.getOrgId());
        return Result.ok();
    }

    @Operation(summary = "管理员查看所有实例", description = "仅ADMIN可访问")
    @GetMapping("/admin/all")
    public Result<Page<UserPageInstance>> adminList(
            @Parameter(description = "页面类型过滤") @RequestParam(required = false) String pageCode,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量，默认20") @RequestParam(defaultValue = "20") int size) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        Page<UserPageInstance> result = instanceService.listAll(pageCode, UserContext.getOrgId(), true, current, size);
        return Result.ok(result);
    }
}
