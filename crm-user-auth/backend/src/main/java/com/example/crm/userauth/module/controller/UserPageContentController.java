package com.example.crm.userauth.module.controller;

import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.entity.UserPageContent;
import com.example.crm.userauth.module.security.UserContext;
import com.example.crm.userauth.module.service.UserPageContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class UserPageContentController {

    private final UserPageContentService contentService;

    @GetMapping("/{pageCode}")
    public Result<String> getContent(@PathVariable String pageCode) {
        Long userId = UserContext.getUserId();
        String content = contentService.get(userId, pageCode);
        return Result.ok(content != null ? content : "");
    }

    @PutMapping("/{pageCode}")
    public Result<Void> saveContent(@PathVariable String pageCode, @RequestBody Map<String, String> body) {
        Long userId = UserContext.getUserId();
        contentService.save(userId, pageCode, body.get("content"), userId);
        return Result.ok();
    }

    @GetMapping("/{pageCode}/all")
    public Result<List<UserPageContent>> getAllContent(@PathVariable String pageCode) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "无权访问");
        }
        return Result.ok(contentService.listByPage(pageCode));
    }
}
