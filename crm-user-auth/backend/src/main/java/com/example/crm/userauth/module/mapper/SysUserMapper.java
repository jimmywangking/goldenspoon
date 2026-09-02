package com.example.crm.userauth.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.module.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT username FROM sys_user WHERE username = #{username} AND is_deleted = false LIMIT 1")
    String findUsername(@Param("username") String username);

    @Select("SELECT u.id, u.username, u.password, u.real_name, u.role, u.is_active, " +
            "       o.id as org_id, o.name as org_name " +
            "FROM sys_user u LEFT JOIN org o ON u.org_id = o.id " +
            "WHERE u.username = #{username} AND u.is_deleted = false LIMIT 1")
    SysUser findByUsername(@Param("username") String username);

    @Select("<script>" +
            "SELECT u.id, u.username, u.password, u.real_name, u.role, u.is_active, " +
            "       u.email, u.phone, u.org_id, o.name as org_name, " +
            "       u.created_at, u.updated_at, u.created_by, u.is_deleted, u.deleted_at " +
            "FROM sys_user u LEFT JOIN org o ON u.org_id = o.id " +
            "WHERE u.is_deleted = false " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (u.username LIKE CONCAT('%', #{keyword}, '%') OR u.real_name LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='role != null and role != \"\"'>" +
            "AND u.role = #{role}" +
            "</if>" +
            "<if test='orgId != null'>" +
            "AND u.org_id = #{orgId}" +
            "</if>" +
            "ORDER BY u.created_at DESC" +
            "</script>")
    IPage<SysUser> listPage(Page<SysUser> page, @Param("keyword") String keyword,
                            @Param("role") String role, @Param("orgId") Long orgId);
}
