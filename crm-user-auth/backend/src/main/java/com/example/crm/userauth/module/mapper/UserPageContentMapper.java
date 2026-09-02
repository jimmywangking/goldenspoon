package com.example.crm.userauth.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.crm.userauth.module.entity.UserPageContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserPageContentMapper extends BaseMapper<UserPageContent> {

    @Select("SELECT * FROM user_page_content WHERE user_id = #{userId} AND page_code = #{pageCode} AND is_deleted = false LIMIT 1")
    UserPageContent findByUserAndPage(@Param("userId") Long userId, @Param("pageCode") String pageCode);

    @Select("SELECT c.* FROM user_page_content c JOIN sys_user u ON c.user_id = u.id WHERE c.page_code = #{pageCode} AND u.is_deleted = false ORDER BY c.updated_at DESC")
    java.util.List<UserPageContent> listByPage(@Param("pageCode") String pageCode);
}
