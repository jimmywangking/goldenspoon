package com.example.crm.userauth.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("user_page_instance")
public class UserPageInstance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String pageCode;
    private String title;
    private String content;
    private Integer sortOrder;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    private Boolean isDeleted;
    private OffsetDateTime deletedAt;
}
