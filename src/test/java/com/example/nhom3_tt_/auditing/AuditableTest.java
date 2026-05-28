package com.example.nhom3_tt_.auditing;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditableTest {

  private static class TestAuditable extends Auditable {}

  @Test
  void gettersAndSetters_workForAuditFields() {
    TestAuditable auditable = new TestAuditable();
    LocalDateTime createdAt = LocalDateTime.of(2026, 5, 28, 10, 0);
    LocalDateTime updatedAt = LocalDateTime.of(2026, 5, 28, 11, 0);

    auditable.setCreatedAt(createdAt);
    auditable.setUpdatedAt(updatedAt);
    auditable.setCreatedBy(7L);
    auditable.setUpdatedBy(8L);

    assertEquals(createdAt, auditable.getCreatedAt());
    assertEquals(updatedAt, auditable.getUpdatedAt());
    assertEquals(7L, auditable.getCreatedBy());
    assertEquals(8L, auditable.getUpdatedBy());
  }

  @Test
  void lombokMethods_workOnAuditableSubclass() {
    TestAuditable first = new TestAuditable();
    TestAuditable second = new TestAuditable();

    first.setCreatedBy(1L);
    second.setCreatedBy(1L);

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
    assertTrue(first.toString().contains("createdBy=1"));
  }
}