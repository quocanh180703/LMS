package com.example.nhom3_tt_.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class JwtRequestFilterTest {

  @Mock private JwtService jwtService;

  @Mock private UserDetailsService userDetailsService;

  @Mock private FilterChain filterChain;

  @InjectMocks private JwtRequestFilter jwtRequestFilter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldBypassPublicAuthEndpoints() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/v1/auth/login");
    MockHttpServletResponse response = new MockHttpServletResponse();

    jwtRequestFilter.doFilter(request, response, filterChain);

    verifyNoInteractions(jwtService, userDetailsService);
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldBypassWhenAuthorizationHeaderIsMissing() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/v1/courses");
    MockHttpServletResponse response = new MockHttpServletResponse();

    jwtRequestFilter.doFilter(request, response, filterChain);

    verifyNoInteractions(jwtService, userDetailsService);
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldContinueWhenTokenCannotBeParsed() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/v1/courses");
    request.addHeader("Authorization", "Bearer invalid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtService.extractUsername("invalid-token", response)).thenReturn(null);

    jwtRequestFilter.doFilter(request, response, filterChain);

    verify(jwtService).extractUsername("invalid-token", response);
    verifyNoInteractions(userDetailsService);
    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldAuthenticateRequestWhenTokenIsValid() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/v1/courses");
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
    when(jwtService.extractUsername("valid-token", response)).thenReturn("huy_cr311");
    when(userDetailsService.loadUserByUsername("huy_cr311")).thenReturn(userDetails);
    when(jwtService.isTokenValid("valid-token", userDetails, response)).thenReturn(true);

    jwtRequestFilter.doFilter(request, response, filterChain);

    verify(jwtService).extractUsername("valid-token", response);
    verify(userDetailsService).loadUserByUsername("huy_cr311");
    verify(jwtService).isTokenValid("valid-token", userDetails, response);
    verify(filterChain).doFilter(request, response);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(authentication);
    assertTrue(authentication instanceof UsernamePasswordAuthenticationToken);
    assertEquals(userDetails, authentication.getPrincipal());
  }

  @Test
  void shouldNotReloadUserWhenAuthenticationAlreadyExists() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/v1/courses");
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    Authentication existingAuth =
        new UsernamePasswordAuthenticationToken("already-authenticated", null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(existingAuth);

    when(jwtService.extractUsername("valid-token", response)).thenReturn("huy_cr311");

    jwtRequestFilter.doFilter(request, response, filterChain);

    verify(jwtService).extractUsername("valid-token", response);
    verifyNoInteractions(userDetailsService);
    verify(filterChain).doFilter(request, response);
    assertSame(existingAuth, SecurityContextHolder.getContext().getAuthentication());
  }
}