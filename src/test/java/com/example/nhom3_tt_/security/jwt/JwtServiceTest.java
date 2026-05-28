package com.example.nhom3_tt_.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private static final String SECRET =
      "0123456789012345678901234567890123456789012345678901234567890123456789";

  private JwtService jwtService;

  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "SECRET", SECRET);
    ReflectionTestUtils.setField(jwtService, "expiration", 60_000L);

    userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("huy_cr311");
    doReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
      .when(userDetails)
      .getAuthorities();
  }

  @Test
  void generateAccessTokenShouldIncludeUsernameAndRoleClaim() throws Exception {
    String token = jwtService.generateAccessToken(userDetails);

    assertNotNull(token);

    MockHttpServletResponse response = new MockHttpServletResponse();
    assertEquals("huy_cr311", jwtService.extractUsername(token, response));

    Claims claims = parseClaims(token);
    assertEquals("huy_cr311", claims.getSubject());
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
    assertTrue(claims.get("role") instanceof java.util.List);
    assertFalse(((java.util.List<?>) claims.get("role")).isEmpty());
  }

  @Test
  void generateAccessTokenShouldUseDefaultRoleWhenAuthoritiesAreNull() throws Exception {
    doReturn(null).when(userDetails).getAuthorities();

    String token = jwtService.generateAccessToken(userDetails);

    Claims claims = parseClaims(token);
    assertEquals("USER", claims.get("role"));
  }

  @Test
  void isTokenValidShouldReturnTrueForMatchingUser() throws Exception {
    String token = jwtService.generateAccessToken(userDetails);
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertTrue(jwtService.isTokenValid(token, userDetails, response));
    assertEquals(200, response.getStatus());
  }

  @Test
  void isTokenValidShouldReturnFalseWhenUsernameDiffers() throws Exception {
    String token = jwtService.generateAccessToken(userDetails);
    UserDetails otherUser = mock(UserDetails.class);
    when(otherUser.getUsername()).thenReturn("other_user");
    doReturn(Collections.emptyList()).when(otherUser).getAuthorities();

    MockHttpServletResponse response = new MockHttpServletResponse();

    assertFalse(jwtService.isTokenValid(token, otherUser, response));
  }

  @Test
  void extractUsernameShouldReturnNullAndSendUnauthorizedForExpiredToken() throws Exception {
    String expiredToken =
        Jwts.builder()
            .setSubject("huy_cr311")
            .setIssuedAt(new Date(System.currentTimeMillis() - 120_000L))
            .setExpiration(new Date(System.currentTimeMillis() - 60_000L))
            .signWith(getSecretKey(), SignatureAlgorithm.HS512)
            .compact();

    MockHttpServletResponse response = new MockHttpServletResponse();

    assertNull(jwtService.extractUsername(expiredToken, response));
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    assertEquals("Token Expired!", response.getErrorMessage());
  }

  @Test
  void extractUsernameShouldReturnNullAndSendUnauthorizedForMalformedToken() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertNull(jwtService.extractUsername("not-a-valid-token", response));
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    assertEquals("Token Invalid!", response.getErrorMessage());
  }

  private Claims parseClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSecretKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private SecretKey getSecretKey() {
    return io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET.getBytes());
  }
}