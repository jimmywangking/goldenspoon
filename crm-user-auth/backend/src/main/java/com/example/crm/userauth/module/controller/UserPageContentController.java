package com.example.crm.userauth.module.controller;

import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.entity.UserPageContent;
import com.example.crm.userauth.module.security.UserContext;
import com.example.crm.userauth.module.service.UserPageContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "页面内容", description = "用户页面JSON内容的读写操作，支持版本控制")
@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class UserPageContentController {

    private final UserPageContentService contentService;

    @Operation(summary = "获取当前用户的页面内容")
    @GetMapping("/{pageCode}")
    public Result<String> getContent(@PathVariable String pageCode) {
        Long userId = UserContext.getUserId();
        String content = contentService.get(userId, pageCode);
        return Result.ok(content != null ? content : "");
    }

    @Operation(summary = "保存页面内容（创建新版本）")
    @PostMapping("/{pageCode}")
    public Result<Void> saveContent(@PathVariable String pageCode, @RequestBody Map<String, String> body) {
        Long userId = UserContext.getUserId();
        String content = body.get("content");
        String versionName = body.get("versionName");
        contentService.save(userId, pageCode, content, userId, versionName);
        return Result.ok();
    }

    @Operation(summary = "查看当前用户的版本历史")
    @GetMapping("/{pageCode}/versions")
    public Result<List<UserPageContent>> listVersions(@PathVariable String pageCode) {
        Long userId = UserContext.getUserId();
        return Result.ok(contentService.listVersions(userId, pageCode));
    }

    @Operation(summary = "恢复到指定版本")
    @PostMapping("/{pageCode}/versions/{targetId}/restore")
    public Result<Void> restoreVersion(@PathVariable String pageCode, @PathVariable Long targetId) {
        Long userId = UserContext.getUserId();
        contentService.restore(userId, pageCode, targetId);
        return Result.ok();
    }

    @Operation(summary = "查看所有用户页面内容")
    @GetMapping("/{pageCode}/all")
    public Result<List<UserPageContent>> getAllContent(@PathVariable String pageCode) {
        if (!UserContext.isAdmin()) {
            if (!UserContext.isOrgAdmin()) {
                return Result.fail(403, "无权访问");
            }
        }
        return Result.ok(contentService.listByPage(pageCode, UserContext.isAdmin(), UserContext.getOrgId()));
    }
}
