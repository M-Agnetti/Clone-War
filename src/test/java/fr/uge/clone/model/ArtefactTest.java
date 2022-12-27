package fr.uge.clone.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArtefactTest {
  @Nested
  public class Construction {
    @Test
    public void getFields(){
      Artefact artefact = new Artefact(0, "a1", new Date(1672144775L), 0, "");
      assertEquals(0, artefact.id());
      assertEquals("a1", artefact.name());
      assertEquals(new Date(1672144775L), artefact.dateAdd());
      assertEquals(0, artefact.analyzing());
      assertEquals("", artefact.url());
    }
  }
}
