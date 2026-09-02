package com.example.crm.userauth.module.controller;

import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.dto.LoginRequest;
import com.example.crm.userauth.module.dto.LoginResponse;
import com.example.crm.userauth.module.entity.SysUser;
import com.example.crm.userauth.module.security.JwtUtils;
import com.example.crm.userauth.module.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestParam String refreshToken) {
        // TODO: validate refresh token in Redis blacklist
        LoginRequest dummy = new LoginRequest();
        // This is a simplified implementation - in production, validate and re-issue tokens
        return Result.ok(new LoginResponse("refreshed", "refreshed-token", 86400L, null));
    }

    @GetMapping("/me")
    public Result<LoginResponse.UserInfo> me(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.fail(401, "未授权");
        }
        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.fail(401, "Token无效或已过期");
        }
        String username = jwtUtils.getUsernameFromToken(token);
        SysUser user = authService.getCurrentUser(username);
        if (user == null || !user.getIsActive()) {
            return Result.fail(401, "用户不存在或已禁用");
        }
        return Result.ok(authService.buildUserInfo(user));
    }
}
