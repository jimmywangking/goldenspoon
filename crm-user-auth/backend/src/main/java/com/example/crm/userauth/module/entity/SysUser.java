package com.example.crm.userauth.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String phone;
    private String realName;
    private Long orgId;

    @TableField(exist = false)
    private String orgName;

    private String role;
    private Boolean isOrgAdmin;
    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    private Long createdBy;

    private Boolean isDeleted;

    private OffsetDateTime deletedAt;
}
