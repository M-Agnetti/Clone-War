package fr.uge.clone.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScoreTest {
  @Nested
  public class Construction {
    @Test
    public void getFields(){
      Score score = new Score(1, 2, 100);
      assertEquals(1, score.id1());
      assertEquals(2, score.id2());
      assertEquals(100, score.score());
    }
  }
}
