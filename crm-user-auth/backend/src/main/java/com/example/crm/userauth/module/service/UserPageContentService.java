package com.example.crm.userauth.module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.crm.userauth.module.entity.UserPageContent;
import com.example.crm.userauth.module.mapper.UserPageContentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPageContentService {

    private final UserPageContentMapper contentMapper;

    public String get(Long userId, String pageCode) {
        UserPageContent content = contentMapper.findByUserAndPage(userId, pageCode);
        return content != null ? content.getContent() : null;
    }

    public void save(Long userId, String pageCode, String content, Long operatorId) {
        LambdaQueryWrapper<UserPageContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPageContent::getUserId, userId).eq(UserPageContent::getPageCode, pageCode);
        UserPageContent entity = contentMapper.selectOne(wrapper);
        if (entity == null) {
            entity = new UserPageContent();
            entity.setUserId(userId);
            entity.setPageCode(pageCode);
            entity.setContent(content);
            entity.setUpdatedBy(operatorId);
            contentMapper.insert(entity);
        } else {
            entity.setContent(content);
            entity.setUpdatedBy(operatorId);
            contentMapper.updateById(entity);
        }
    }

    public List<UserPageContent> listByPage(String pageCode) {
        return contentMapper.listByPage(pageCode);
    }
}
