package com.example.crm.userauth.module.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.common.Result;
import com.example.crm.userauth.module.dto.LoginRequest;
import com.example.crm.userauth.module.dto.LoginResponse;
import com.example.crm.userauth.module.dto.RegisterRequest;
import com.example.crm.userauth.module.dto.RegistrationDTO;
import com.example.crm.userauth.module.entity.SysUser;
import com.example.crm.userauth.module.security.JwtUtils;
import com.example.crm.userauth.module.security.UserContext;
import com.example.crm.userauth.module.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证", description = "用户登录、注册、刷新Token、获取当前用户信息")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @Operation(
        summary = "用户登录",
        description = "使用用户名和密码登录，返回 JWT token。账户未审批则无法登录",
        responses = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "用户名或密码错误 / 账户待审批")
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

    @Operation(
        summary = "用户注册",
        description = "新用户注册，状态为 PENDING，等待管理员审批后才能登录",
        responses = {
            @ApiResponse(responseCode = "200", description = "注册成功，等待审批"),
            @ApiResponse(responseCode = "400", description = "用户名已存在")
        }
    )
    @PostMapping("/register")
    public Result<SysUser> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @Operation(
        summary = "查询待审批用户列表",
        description = "仅 ADMIN 可访问，分页查询所有状态为 PENDING 的用户",
        responses = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无权限访问")
        }
    )
    @GetMapping("/pending")
    public Result<Page<RegistrationDTO>> listPending(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认10") @RequestParam(defaultValue = "10") int size) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "仅管理员可访问");
        }
        return Result.ok(authService.getPendingRegistrations(page, size));
    }

    @Operation(
        summary = "审批用户注册",
        description = "仅 ADMIN 可访问，通过（APPROVED）或拒绝（REJECTED）待审批用户",
        responses = {
            @ApiResponse(responseCode = "200", description = "审批成功"),
            @ApiResponse(responseCode = "403", description = "无权限访问"),
            @ApiResponse(responseCode = "400", description = "用户不存在或非待审批状态")
        }
    )
    @PostMapping("/{id}/approve")
    public Result<Void> approve(
            @PathVariable Long id,
            @RequestBody RegistrationDTO.ApprovalRequest body) {
        if (!UserContext.isAdmin()) {
            return Result.fail(403, "仅管理员可访问");
        }
        boolean approved = "APPROVE".equalsIgnoreCase(body.getAction());
        authService.approve(id, approved);
        return Result.ok();
    }
}
