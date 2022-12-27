package fr.uge.clone.analyze;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CloneDetectorTest {
  @Nested
  public class Construction {
    @Test
    public void shouldGetAnErrorWhenDbClientIsNull() {
      assertThrows(NullPointerException.class, () -> new CloneDetector(null, 0));
    }
  }
}
