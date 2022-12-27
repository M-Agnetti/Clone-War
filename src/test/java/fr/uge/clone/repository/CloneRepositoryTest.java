package fr.uge.clone.repository;

import fr.uge.clone.model.Artefact;
import fr.uge.clone.model.MetaData;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CloneRepositoryTest {
  @Nested
  public class Construction {
    @Test
    public void shouldGetAnErrorWhenDbClientIsNull() {
      assertThrows(NullPointerException.class, () -> new CloneRepository(null));
    }
  }

  @Nested
  public class DataBase {

    Config dbConfig = Config.create().get("db");
    DbClient dbClient = DbClient.create(dbConfig);
    {
      dbClient.execute(exec -> exec
              .namedDml("create-jar")).await();
      dbClient.execute(exec -> exec
              .namedDml("create-artefact")).await();
      dbClient.execute(exec -> exec
              .namedDml("create-instruction")).await();
      dbClient.execute(exec -> exec
              .namedDml("create-metadata")).await();
      dbClient.execute(exec -> exec
              .namedDml("create-clone")).await();
      dbClient.execute(exec -> exec
              .namedDml("create-score")).await();
    }

    @Test
    public void InsertAndGetArtefact() {
      CloneRepository repo = new CloneRepository(dbClient);
      assertThrows(NullPointerException.class, () -> repo.insertArtefact(0, null, "testUrl"));
      assertThrows(NullPointerException.class, () -> repo.insertArtefact(0, "test", null));

      repo.insertArtefact(0, "test", "testUrl");
      assertEquals(
              new Artefact(0, "test", Date.valueOf(LocalDate.now()), 0, "testUrl"),
              repo.selectArtById(0));
      assertEquals(
              List.of(new Artefact(0, "test", Date.valueOf(LocalDate.now()), 0, "testUrl")),
              repo.selectAllArtefacts());

    }

    @Test
    public void InsertAndGetMetaData() {
      CloneRepository repo = new CloneRepository(dbClient);
      assertThrows(NullPointerException.class,
              () -> repo.insertMetadata(0, null, "model", "0.0.1-SNAPSHOT"));
      assertThrows(NullPointerException.class,
              () -> repo.insertMetadata(0, "fr.uge.model", null, "0.0.1-SNAPSHOT"));
      assertThrows(NullPointerException.class,
              () -> repo.insertMetadata(0, "fr.uge.model", "model", null));
      repo.insertMetadata(0, "fr.uge.model", "model", "0.0.1-SNAPSHOT");
      assertEquals(
              new MetaData(0, "fr.uge.model", "model", "0.0.1-SNAPSHOT"),
              repo.selectMetaDataById(0).orElseThrow());
    }
  }
}
