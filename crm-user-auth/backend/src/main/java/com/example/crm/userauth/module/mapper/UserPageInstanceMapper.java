package com.example.crm.userauth.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.crm.userauth.module.entity.UserPageInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserPageInstanceMapper extends BaseMapper<UserPageInstance> {

    @Select("SELECT COUNT(*) FROM user_page_instance "
            + "WHERE user_id = #{userId} AND page_code = #{pageCode} AND is_deleted = false")
    long countByUserAndPageCode(@Param("userId") Long userId,
                                @Param("pageCode") String pageCode);

    @Select("<script>"
            + "SELECT i.*, u.org_id FROM user_page_instance i "
            + "JOIN sys_user u ON i.user_id = u.id "
            + "WHERE i.page_code = #{pageCode} AND i.is_deleted = false "
            + "<if test='orgId != null'>AND u.org_id = #{orgId}</if> "
            + "ORDER BY i.updated_at DESC"
            + "</script>")
    List<UserPageInstance> listAllByPage(@Param("pageCode") String pageCode,
                                         @Param("orgId") Long orgId);

    @Select("<script>"
            + "SELECT COUNT(*) FROM user_page_instance i "
            + "JOIN sys_user u ON i.user_id = u.id "
            + "WHERE i.page_code = #{pageCode} AND i.is_deleted = false "
            + "<if test='orgId != null'>AND u.org_id = #{orgId}</if>"
            + "</script>")
    long countAllByPage(@Param("pageCode") String pageCode,
                        @Param("orgId") Long orgId);
}
