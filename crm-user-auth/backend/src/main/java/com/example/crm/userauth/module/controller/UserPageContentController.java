package com.example.crm.userauth.module.controller;

import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.entity.UserPageContent;
import com.example.crm.userauth.module.security.UserContext;
import com.example.crm.userauth.module.service.UserPageContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "页面内容", description = "用户页面JSON内容的读写操作")
@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class UserPageContentController {

    private final UserPageContentService contentService;

    @Operation(summary = "获取当前用户的页面内容", description = "返回该用户该页面的JSON内容字符串")
    @GetMapping("/{pageCode}")
    public Result<String> getContent(@PathVariable String pageCode) {
        Long userId = UserContext.getUserId();
        String content = contentService.get(userId, pageCode);
        return Result.ok(content != null ? content : "");
    }

    @Operation(
        summary = "保存页面内容",
        description = "覆盖保存当前用户的页面JSON内容",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "页面内容，格式: {\"content\": \"JSON字符串\"}"
        )
    )
    @PutMapping("/{pageCode}")
    public Result<Void> saveContent(@PathVariable String pageCode, @RequestBody Map<String, String> body) {
        Long userId = UserContext.getUserId();
        contentService.save(userId, pageCode, body.get("content"), userId);
        return Result.ok();
    }

    @Operation(summary = "管理员查看某页面所有用户的内容")
    @GetMapping("/{pageCode}/all")
    public Result<List<UserPageContent>> getAllContent(@PathVariable String pageCode) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        return Result.ok(contentService.listByPage(pageCode));
    }
}
