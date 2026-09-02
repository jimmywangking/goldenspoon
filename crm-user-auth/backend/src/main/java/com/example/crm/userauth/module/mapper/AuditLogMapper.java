package com.example.crm.userauth.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.crm.userauth.module.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
