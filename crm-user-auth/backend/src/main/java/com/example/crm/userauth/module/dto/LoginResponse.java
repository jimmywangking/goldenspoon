package com.example.crm.userauth.module.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private UserInfo user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String role;
        private Long orgId;
        private String orgName;
        private boolean isAdmin;
        private PagePermission[] permissions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagePermission {
        private String pageCode;
        private boolean canView;
        private boolean canEdit;
    }
}
