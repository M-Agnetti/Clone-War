package fr.uge.clone.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CloneArtefactTest {
  @Nested
  public class Construction {
    @Test
    public void getFields(){
      Artefact artefact = new Artefact(0, "a1", new Date(1672144775L), 0, "");
      CloneArtefact clone = new CloneArtefact(artefact, 0);
      assertEquals(new Artefact(0, "a1", new Date(1672144775L), 0, ""), clone.artefact());
      assertEquals(0, clone.score());
    }
  }
}
