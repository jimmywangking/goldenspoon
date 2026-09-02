package com.example.crm.userauth.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("role_page_permission")
public class RolePagePermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roleId;
    private String pageCode;
    private Boolean canView;
    private Boolean canEdit;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
