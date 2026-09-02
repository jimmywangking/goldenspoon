package com.example.crm.userauth.module.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.crm.userauth.module.entity.Org;
import com.example.crm.userauth.module.mapper.OrgMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrgServiceTest {

    @Mock
    private OrgMapper orgMapper;

    @InjectMocks
    private OrgService orgService;

    @Test
    void create_org() {
        Org org = new Org();
        org.setName("测试公司");
        when(orgMapper.insert(any(Org.class))).thenReturn(1);

        Org result = orgService.create(org);

        assertThat(result.getName()).isEqualTo("测试公司");
        verify(orgMapper).insert(org);
    }

    @Test
    void getOrg_notFound() {
        when(orgMapper.selectById(999L)).thenReturn(null);
        assertThat(orgService.getById(999L)).isNull();
    }

    @Test
    void delete_org() {
        Org org = new Org();
        org.setId(1L);
        org.setIsDeleted(false);
        when(orgMapper.selectById(1L)).thenReturn(org);

        orgService.delete(1L);

        verify(orgMapper).updateById(org);
        assertThat(org.getIsDeleted()).isTrue();
    }

    @Test
    void delete_org_notFound_throws() {
        when(orgMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> orgService.delete(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("组织不存在");
    }
}
