package com.example.nhom3_tt_.auditing;

import com.example.nhom3_tt_.models.Category;
import com.example.nhom3_tt_.models.User;
import com.example.nhom3_tt_.repositories.CategoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.boot.test.context.TestConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(AuditableJpaTest.AuditingTestConfig.class)
class AuditableJpaTest {

  @Autowired private CategoryRepository categoryRepository;

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void savingCategory_populatesAuditFields() {
    User auditor = new User();
    auditor.setId(99L);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                auditor, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

    Category category = Category.builder().name("Programming").build();
    Category saved = categoryRepository.saveAndFlush(category);

    assertNotNull(saved.getCreatedAt());
    assertNotNull(saved.getUpdatedAt());
    assertEquals(99L, saved.getCreatedBy());
    assertEquals(99L, saved.getUpdatedBy());

    LocalDateTime createdAt = saved.getCreatedAt();
    saved.setName("Programming Updated");
    Category updated = categoryRepository.saveAndFlush(saved);

    assertEquals(createdAt, updated.getCreatedAt());
    assertNotNull(updated.getUpdatedAt());
    assertEquals(99L, updated.getCreatedBy());
    assertEquals(99L, updated.getUpdatedBy());
  }

  @TestConfiguration
  @EnableJpaAuditing(auditorAwareRef = "auditorAware")
  static class AuditingTestConfig {

    @Bean
    AuditorAware<Long> auditorAware() {
      return new ApplicationAuditAware();
    }
  }
}