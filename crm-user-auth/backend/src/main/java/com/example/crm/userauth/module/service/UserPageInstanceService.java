package com.example.crm.userauth.module.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.module.entity.SysUser;
import com.example.crm.userauth.module.entity.UserPageInstance;
import com.example.crm.userauth.module.mapper.SysUserMapper;
import com.example.crm.userauth.module.mapper.UserPageInstanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPageInstanceService {

    private static final int USER_INSTANCE_LIMIT = 5;
    private static final int ORG_ADMIN_INSTANCE_LIMIT = 20;

    private final UserPageInstanceMapper instanceMapper;
    private final SysUserMapper sysUserMapper;

    public Page<UserPageInstance> listByUser(Long userId, String pageCode, int current, int size) {
        Page<UserPageInstance> page = new Page<>(current, size);
        List<UserPageInstance> records = instanceMapper.listByUser(userId, pageCode, current, size);
        long total = instanceMapper.countByUserAndPageCode(userId, pageCode);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    public Page<UserPageInstance> listAll(String pageCode, Long operatorOrgId, boolean isAdmin, int current, int size) {
        if (!isAdmin) {
            throw new IllegalStateException("仅管理员可访问");
        }
        Long orgId = isAdmin ? null : operatorOrgId;
        long total = instanceMapper.countAllByPage(pageCode, orgId);
        List<UserPageInstance> records = instanceMapper.listAllByPage(pageCode, orgId, current, size);
        Page<UserPageInstance> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    public UserPageInstance getById(Long id, Long operatorId, String operatorRole, Long operatorOrgId) {
        UserPageInstance instance = instanceMapper.selectById(id);
        if (instance == null || Boolean.TRUE.equals(instance.getIsDeleted())) {
            throw new IllegalArgumentException("实例不存在");
        }
        if ("ADMIN".equals(operatorRole)) {
            return instance;
        }
        if ("ORG_ADMIN".equals(operatorRole)) {
            SysUser owner = sysUserMapper.selectById(instance.getUserId());
            if (owner == null || !operatorOrgId.equals(owner.getOrgId())) {
                throw new IllegalStateException("无权访问该实例");
            }
            return instance;
        }
        if (!operatorId.equals(instance.getUserId())) {
            throw new IllegalStateException("无权访问该实例");
        }
        return instance;
    }

    public UserPageInstance create(UserPageInstance instance, Long operatorId, String operatorRole, Long operatorOrgId) {
        Long userId = instance.getUserId();
        String pageCode = instance.getPageCode();
        long count = instanceMapper.countByUserAndPageCode(userId, pageCode);

        int limit = "USER".equals(operatorRole) ? USER_INSTANCE_LIMIT
                  : "ORG_ADMIN".equals(operatorRole) ? ORG_ADMIN_INSTANCE_LIMIT : Integer.MAX_VALUE;
        if (count >= limit) {
            throw new IllegalStateException("超出页面实例数量限制（最多" + limit + "个）");
        }
        return instanceMapper.insert(instance) > 0 ? instance : null;
    }

    public void update(Long id, String title, String content, Long operatorId, String operatorRole, Long operatorOrgId) {
        UserPageInstance instance = getById(id, operatorId, operatorRole, operatorOrgId);
        if (title != null) instance.setTitle(title);
        if (content != null) instance.setContent(content);
        instanceMapper.updateById(instance);
    }

    public void softDelete(Long id, Long operatorId, String operatorRole, Long operatorOrgId) {
        UserPageInstance instance = getById(id, operatorId, operatorRole, operatorOrgId);
        instance.setIsDeleted(true);
        instance.setDeletedAt(java.time.OffsetDateTime.now());
        instanceMapper.updateById(instance);
    }
}
