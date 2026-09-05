package com.example.crm.userauth.module.service;

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

    public void save(Long userId, String pageCode, String content, Long operatorId, String versionName) {
        int nextVersion = contentMapper.countByUserAndPage(userId, pageCode) + 1;
        UserPageContent entity = new UserPageContent();
        entity.setUserId(userId);
        entity.setPageCode(pageCode);
        entity.setContent(content);
        entity.setUpdatedBy(operatorId);
        entity.setVersion(nextVersion);
        entity.setVersionName(versionName);
        contentMapper.insert(entity);
    }

    public void restore(Long userId, String pageCode, Long targetId) {
        UserPageContent target = contentMapper.selectById(targetId);
        if (target == null || !target.getUserId().equals(userId) || target.getDeletedAt() != null) {
            throw new IllegalArgumentException("版本不存在或无权限");
        }
        UserPageContent latest = contentMapper.findByUserAndPage(userId, pageCode);
        if (latest != null) {
            latest.setContent(target.getContent());
            latest.setVersionName("恢复到版本 " + target.getVersion());
            contentMapper.updateById(latest);
        }
    }

    public List<UserPageContent> listVersions(Long userId, String pageCode) {
        return contentMapper.listVersions(userId, pageCode);
    }

    public List<UserPageContent> listByPage(String pageCode, boolean isAdmin, Long orgId) {
        if (isAdmin) {
            return contentMapper.listByPage(pageCode);
        }
        return contentMapper.listByPageAndOrg(pageCode, orgId);
    }
}
