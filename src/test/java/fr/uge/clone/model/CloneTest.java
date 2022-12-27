package fr.uge.clone.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CloneTest {
  @Nested
  public class Construction {
    @Test
    public void getFields(){
      Clone clone = new Clone(0, 1, 2);
      assertEquals(0, clone.idClone());
      assertEquals(1, clone.id1());
      assertEquals(2, clone.id2());
    }
  }
}
