package fr.uge.clone.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InstructionTest {
  @Nested
  public class Construction {
    @Test
    public void shouldGetAnErrorWhenFileIsNull(){
      assertThrows(NullPointerException.class, () -> new Instruction(0, 0, null, 1, 0));
    }

    @Test
    public void getFields(){
      Instruction instruction = new Instruction(0, 0, "test.jar", 1, 0);
      assertEquals(0, instruction.idHash());
      assertEquals(0, instruction.hash());
      assertEquals("test.jar", instruction.file());
      assertEquals(1, instruction.line());
      assertEquals(0, instruction.id());
    }
  }
}
