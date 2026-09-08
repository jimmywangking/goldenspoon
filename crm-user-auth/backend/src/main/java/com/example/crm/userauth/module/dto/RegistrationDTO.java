package com.example.crm.userauth.module.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDTO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private Long orgId;
    private String orgName;
    private String status;
    private OffsetDateTime createdAt;
    private List<String> messages;

    public static RegistrationDTO fromEntity(com.example.crm.userauth.module.entity.SysUser u) {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setRealName(u.getRealName());
        dto.setEmail(u.getEmail());
        dto.setPhone(u.getPhone());
        dto.setOrgId(u.getOrgId());
        dto.setOrgName(u.getOrgName());
        dto.setStatus(u.getStatus());
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalRequest {
        private String action; // APPROVE or REJECT
    }
}
