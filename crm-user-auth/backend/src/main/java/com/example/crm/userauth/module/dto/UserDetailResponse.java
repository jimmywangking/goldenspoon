package com.example.crm.userauth.module.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class UserDetailResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String realName;
    private Long orgId;
    private String orgName;
    private String role;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private List<PagePermissionDTO> permissions;

    public UserDetailResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public List<PagePermissionDTO> getPermissions() { return permissions; }
    public void setPermissions(List<PagePermissionDTO> permissions) { this.permissions = permissions; }

    public static class PagePermissionDTO {
        private String pageCode;
        private Boolean canView;
        private Boolean canEdit;

        public PagePermissionDTO() {}
        public PagePermissionDTO(String pageCode, Boolean canView, Boolean canEdit) {
            this.pageCode = pageCode;
            this.canView = canView;
            this.canEdit = canEdit;
        }

        public String getPageCode() { return pageCode; }
        public void setPageCode(String pageCode) { this.pageCode = pageCode; }
        public Boolean getCanView() { return canView; }
        public void setCanView(Boolean canView) { this.canView = canView; }
        public Boolean getCanEdit() { return canEdit; }
        public void setCanEdit(Boolean canEdit) { this.canEdit = canEdit; }
    }
}
