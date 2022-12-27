package fr.uge.clone.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CloneServiceTest {
  @Nested
  public class Construction {
    @Test
    public void shouldGetAnErrorWhenDbClientIsNull() {
      assertThrows(NullPointerException.class, null);
    }
  }
}
