package com.example.crm.userauth.module.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.crm.userauth.module.entity.UserPageContent;
import com.example.crm.userauth.module.mapper.UserPageContentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPageContentServiceTest {

    @Mock
    private UserPageContentMapper contentMapper;

    @InjectMocks
    private UserPageContentService contentService;

    @Test
    void get_existingContent() {
        UserPageContent content = new UserPageContent();
        content.setContent("{\"key\":\"value\"}");
        when(contentMapper.findByUserAndPage(1L, "PAGE_1")).thenReturn(content);

        String result = contentService.get(1L, "PAGE_1");

        assertThat(result).isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    void get_noContent_returnsNull() {
        when(contentMapper.findByUserAndPage(1L, "PAGE_1")).thenReturn(null);

        String result = contentService.get(1L, "PAGE_1");

        assertThat(result).isNull();
    }

    @Test
    void save_newEntry_inserts() {
        contentService.save(1L, "PAGE_1", "{\"a\":1}", 2L);

        verify(contentMapper).insert(any(UserPageContent.class));
    }

    @Test
    void save_existingEntry_updates() {
        UserPageContent existing = new UserPageContent();
        existing.setId(1L);
        when(contentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        contentService.save(1L, "PAGE_1", "{\"b\":2}", 3L);

        verify(contentMapper).updateById(existing);
        assertThat(existing.getContent()).isEqualTo("{\"b\":2}");
    }

    @Test
    void save_newEntry_setsFields() {
        contentService.save(1L, "PAGE_1", "hello", 2L);

        verify(contentMapper).insert(argThat(entity ->
                entity.getUserId() == 1L
                && "PAGE_1".equals(entity.getPageCode())
                && "hello".equals(entity.getContent())
                && entity.getUpdatedBy() == 2L
        ));
    }

    @Test
    void listByPage_returnsAll() {
        List<UserPageContent> expected = List.of(new UserPageContent(), new UserPageContent());
        when(contentMapper.listByPage("PAGE_1")).thenReturn(expected);

        List<UserPageContent> result = contentService.listByPage("PAGE_1");

        assertThat(result).hasSize(2);
    }
}
