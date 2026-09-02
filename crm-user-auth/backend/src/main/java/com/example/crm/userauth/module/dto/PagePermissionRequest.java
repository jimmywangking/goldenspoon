package com.example.crm.userauth.module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PagePermissionRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private List<PagePermissionItem> permissions;

    @Data
    public static class PagePermissionItem {
        @NotBlank(message = "页面代码不能为空")
        private String pageCode;
        private Boolean canView;
        private Boolean canEdit;
    }
}
