package fr.uge.clone.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JarTest {
  @Nested
  public class Construction {
    @Test
    public void getFields(){
      Jar jar = new Jar(0, null, null);
      assertEquals(0, jar.idJar());
      assertNull(jar.classes());
      assertNull(jar.sources());

    }
  }
}
