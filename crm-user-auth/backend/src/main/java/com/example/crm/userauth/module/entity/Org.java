package com.example.crm.userauth.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("org")
public class Org {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String contactName;
    private String contactPhone;
    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    private Boolean isDeleted;

    private OffsetDateTime deletedAt;
}
