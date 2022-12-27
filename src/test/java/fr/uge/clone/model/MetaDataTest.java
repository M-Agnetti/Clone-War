package fr.uge.clone.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetaDataTest {
  @Nested
  public class Construction {
    @Test
    public void getFields(){
      MetaData metaData = new MetaData(0, "fr.uge.model", "model", "0.0.1-SNAPSHOT");
      assertEquals(0, metaData.idMeta());
      assertEquals("fr.uge.model", metaData.groupId());
      assertEquals("model", metaData.artifactId());
      assertEquals("0.0.1-SNAPSHOT", metaData.version());
    }
  }
}
