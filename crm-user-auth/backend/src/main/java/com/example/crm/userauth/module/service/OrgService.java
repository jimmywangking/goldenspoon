package com.example.crm.userauth.module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.module.entity.Org;
import com.example.crm.userauth.module.mapper.OrgMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OrgService {

    private final OrgMapper orgMapper;

    public Page<Org> list(int page, int size, String keyword, Long orgId) {
        Page<Org> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Org> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Org::getIsDeleted, false);
        if (orgId != null) {
            wrapper.eq(Org::getId, orgId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Org::getName, keyword);
        }
        wrapper.orderByDesc(Org::getCreatedAt);
        return orgMapper.selectPage(pageParam, wrapper);
    }

    public Org getById(Long id) {
        Org org = orgMapper.selectById(id);
        if (org == null || org.getIsDeleted()) {
            return null;
        }
        return org;
    }

    public Org create(Org org) {
        orgMapper.insert(org);
        return org;
    }

    public void update(Org org) {
        orgMapper.updateById(org);
    }

    public void delete(Long id) {
        Org org = orgMapper.selectById(id);
        if (org == null || org.getIsDeleted()) {
            throw new IllegalArgumentException("组织不存在");
        }
        org.setIsDeleted(true);
        org.setDeletedAt(java.time.OffsetDateTime.now());
        orgMapper.updateById(org);
    }
}
