package com.example.crm.userauth.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("audit_log")
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long operatorId;
    private String action;
    private String targetType;
    private Long targetId;
    private String detail;
    private String ipAddress;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
