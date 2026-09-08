package com.example.crm.userauth.module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度6-100位")
    private String password;

    @Size(max = 100, message = "邮箱长度不超过100")
    private String email;

    @Size(max = 20, message = "手机号长度不超过20")
    private String phone;

    @Size(max = 50, message = "姓名长度不超过50")
    private String realName;

    private Long orgId;
}
