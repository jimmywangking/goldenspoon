package com.example.crm.userauth.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("user_page_content")
public class UserPageContent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String pageCode;
    private String content;
    private Long updatedBy;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String orgName;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
