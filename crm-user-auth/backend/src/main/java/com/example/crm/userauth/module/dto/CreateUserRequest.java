package com.example.crm.userauth.module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名最长50个字符")
    private String username;

    @Size(min = 6, max = 100, message = "密码长度6-100位")
    private String password;

    private String email;
    private String phone;

    @Size(max = 50, message = "真实姓名最长50个字符")
    private String realName;

    private Long orgId;

    @NotBlank(message = "角色不能为空")
    private String role;
}
