package com.example.crm.userauth.module.security;

import com.example.crm.userauth.module.mapper.SysUserMapper;
import com.example.crm.userauth.module.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private SecurityFilter securityFilter;

    @Test
    void filter_withValidToken_setsAuthentication() throws Exception {
        String token = "fake.jwt.token";
        SysUser user = new SysUser();
        user.setUsername("admin");
        user.setIsActive(true);
        user.setRole("ADMIN");

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(token)).thenReturn("admin");
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(1L);
        when(jwtUtils.getRoleFromToken(token)).thenReturn("ADMIN");
        when(sysUserMapper.findByUsername("admin")).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        request.setRequestURI("/api/orgs");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        securityFilter.doFilter(request, response, chain);

        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(user);
    }

    @Test
    void filter_withoutToken_passesThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        securityFilter.doFilter(request, response, chain);

        // Should not throw, just pass through
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
