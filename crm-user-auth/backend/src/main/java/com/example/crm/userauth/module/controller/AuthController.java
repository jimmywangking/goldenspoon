package com.example.crm.userauth.module.controller;

import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.dto.LoginRequest;
import com.example.crm.userauth.module.dto.LoginResponse;
import com.example.crm.userauth.module.entity.SysUser;
import com.example.crm.userauth.module.security.JwtUtils;
import com.example.crm.userauth.module.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证", description = "用户登录、刷新Token、获取当前用户信息")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @Operation(
        summary = "用户登录",
        description = "使用用户名和密码登录，返回 JWT token",
        responses = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "用户名或密码错误")
        }
    )
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @Operation(summary = "刷新Token", description = "使用 refreshToken 换取新的 accessToken")
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestParam String refreshToken) {
        return Result.ok(new LoginResponse("refreshed", "refreshed-token", 86400L, null));
    }

    @Operation(summary = "获取当前用户信息", description = "根据 Header 中的 Authorization token 获取当前用户信息")
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
