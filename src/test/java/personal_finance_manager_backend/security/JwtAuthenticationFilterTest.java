package personal_finance_manager_backend.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;

import personal_finance_manager_backend.model.User;
import personal_finance_manager_backend.service.UserService;
import personal_finance_manager_backend.repository.TokenRevocationRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserService userService;
    @Mock private TokenRevocationRepository tokenRevocationRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validActiveUserTokenSetsAuthenticationAndRole() throws Exception {
        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(jwtService, userService, tokenRevocationRepository);

        User user = new User("Test User", "test@example.com", "hash", "KES");
        user.setRole("USER");
        user.setActive(true);

        when(tokenRevocationRepository.isRevoked("valid-token")).thenReturn(false);
        when(jwtService.extractEmail("valid-token")).thenReturn("test@example.com");
        when(userService.findUserByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void suspendedUserTokenDoesNotSetAuthentication() throws Exception {
        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(jwtService, userService, tokenRevocationRepository);

        User user = new User("Test User", "test@example.com", "hash", "KES");
        user.setRole("USER");
        user.setActive(false);

        when(tokenRevocationRepository.isRevoked("old-token")).thenReturn(false);
        when(jwtService.extractEmail("old-token")).thenReturn("test@example.com");
        when(userService.findUserByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer old-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void invalidTokenDoesNotSetAuthentication() throws Exception {
        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(jwtService, userService, tokenRevocationRepository);

        when(tokenRevocationRepository.isRevoked("bad-token")).thenReturn(false);
        when(jwtService.extractEmail("bad-token"))
                .thenThrow(new IllegalArgumentException("invalid token"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    @Test
    void revokedTokenDoesNotAuthenticateUser() throws Exception {
        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(jwtService, userService, tokenRevocationRepository);
        when(tokenRevocationRepository.isRevoked("revoked-token")).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer revoked-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userService);
    }

}
