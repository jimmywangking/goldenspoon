package com.example.crm.userauth.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("user_page_permission")
public class UserPagePermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String pageCode;
    private Boolean canView;
    private Boolean canEdit;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    private Long createdBy;

    private Boolean isDeleted;

    private OffsetDateTime deletedAt;
}
