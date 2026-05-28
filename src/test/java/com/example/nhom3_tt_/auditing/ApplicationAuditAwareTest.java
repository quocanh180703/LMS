package com.example.nhom3_tt_.auditing;

import com.example.nhom3_tt_.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationAuditAwareTest {

  private final ApplicationAuditAware applicationAuditAware = new ApplicationAuditAware();

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getCurrentAuditor_withoutAuthentication_returnsEmpty() {
    SecurityContextHolder.clearContext();

    assertTrue(applicationAuditAware.getCurrentAuditor().isEmpty());
  }

  @Test
  void getCurrentAuditor_withAnonymousAuthentication_returnsEmpty() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    assertTrue(applicationAuditAware.getCurrentAuditor().isEmpty());
  }

  @Test
  void getCurrentAuditor_withUnauthenticatedAuthentication_returnsEmpty() {
    var authentication = mock(org.springframework.security.core.Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(false);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    assertTrue(applicationAuditAware.getCurrentAuditor().isEmpty());
  }

  @Test
  void getCurrentAuditor_withUserPrincipal_returnsUserId() {
    User user = new User();
    user.setId(42L);
    SecurityContextHolder.getContext()
      .setAuthentication(
        new UsernamePasswordAuthenticationToken(
          user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

    assertEquals(42L, applicationAuditAware.getCurrentAuditor().orElseThrow());
  }

  @Test
  void getCurrentAuditor_withUserWithoutId_returnsEmpty() {
    User user = new User();
    SecurityContextHolder.getContext()
      .setAuthentication(
        new UsernamePasswordAuthenticationToken(
          user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

    assertTrue(applicationAuditAware.getCurrentAuditor().isEmpty());
  }
}