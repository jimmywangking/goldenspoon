package com.example.crm.userauth.module.service;

import com.example.crm.userauth.module.entity.UserPageContent;
import com.example.crm.userauth.module.mapper.UserPageContentMapper;
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
    void save_newEntry_insertsWithVersion() {
        when(contentMapper.countByUserAndPage(1L, "PAGE_1")).thenReturn(0);

        contentService.save(1L, "PAGE_1", "{\"a\":1}", 2L, "v1");

        verify(contentMapper).insert(argThat(entity ->
                entity.getUserId() == 1L
                && "PAGE_1".equals(entity.getPageCode())
                && "{\"a\":1}".equals(entity.getContent())
                && entity.getUpdatedBy() == 2L
                && entity.getVersion() == 1
                && "v1".equals(entity.getVersionName())
        ));
    }

    @Test
    void save_existingEntry_createsNewVersion() {
        when(contentMapper.countByUserAndPage(1L, "PAGE_1")).thenReturn(2);

        contentService.save(1L, "PAGE_1", "{\"b\":2}", 3L, "修改版");

        verify(contentMapper).insert(argThat(entity ->
                entity.getVersion() == 3
                && "修改版".equals(entity.getVersionName())
        ));
    }

    @Test
    void listByPage_admin_returnsAll() {
        List<UserPageContent> expected = List.of(new UserPageContent(), new UserPageContent());
        when(contentMapper.listByPage("PAGE_1")).thenReturn(expected);

        List<UserPageContent> result = contentService.listByPage("PAGE_1", true, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void listByPage_orgAdmin_returnsOrgOnly() {
        List<UserPageContent> expected = List.of(new UserPageContent());
        when(contentMapper.listByPageAndOrg("PAGE_1", 3L)).thenReturn(expected);

        List<UserPageContent> result = contentService.listByPage("PAGE_1", false, 3L);

        assertThat(result).hasSize(1);
    }

    @Test
    void listVersions_returnsAllForUser() {
        List<UserPageContent> versions = List.of(
            createVersion(2, "v2"),
            createVersion(1, "v1")
        );
        when(contentMapper.listVersions(1L, "PAGE_1")).thenReturn(versions);

        List<UserPageContent> result = contentService.listVersions(1L, "PAGE_1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getVersion()).isEqualTo(2);
        assertThat(result.get(1).getVersion()).isEqualTo(1);
    }

    @Test
    void restore_success() {
        UserPageContent target = new UserPageContent();
        target.setId(10L);
        target.setUserId(1L);
        target.setVersion(1);
        target.setContent("{\"old\":\"data\"}");
        target.setDeletedAt(null);

        UserPageContent latest = new UserPageContent();
        latest.setId(11L);
        latest.setVersion(3);
        latest.setContent("{\"current\":\"data\"}");

        when(contentMapper.selectById(10L)).thenReturn(target);
        when(contentMapper.findByUserAndPage(1L, "PAGE_1")).thenReturn(latest);

        contentService.restore(1L, "PAGE_1", 10L);

        verify(contentMapper).updateById(argThat(entity ->
                "恢复到版本 1".equals(entity.getVersionName())
                && "{\"old\":\"data\"}".equals(entity.getContent())
        ));
    }

    @Test
    void restore_notFound_throws() {
        when(contentMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> contentService.restore(1L, "PAGE_1", 99L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void restore_wrongUser_throws() {
        UserPageContent target = new UserPageContent();
        target.setId(10L);
        target.setUserId(99L);
        target.setDeletedAt(null);

        when(contentMapper.selectById(10L)).thenReturn(target);

        assertThatThrownBy(() -> contentService.restore(1L, "PAGE_1", 10L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void restore_softDeleted_throws() {
        UserPageContent target = new UserPageContent();
        target.setId(10L);
        target.setUserId(1L);
        target.setDeletedAt(java.time.OffsetDateTime.now());

        when(contentMapper.selectById(10L)).thenReturn(target);

        assertThatThrownBy(() -> contentService.restore(1L, "PAGE_1", 10L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private UserPageContent createVersion(int v, String name) {
        UserPageContent c = new UserPageContent();
        c.setVersion(v);
        c.setVersionName(name);
        return c;
    }
}
