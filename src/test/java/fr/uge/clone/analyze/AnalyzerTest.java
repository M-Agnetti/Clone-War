package fr.uge.clone.analyze;

import io.helidon.common.reactive.Single;
import io.helidon.common.reactive.Subscribable;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbExecute;
import io.helidon.dbclient.DbTransaction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AnalyzerTest {
  @Nested
  public class Construction {
/*
    @Test
    public void shouldGetAnErrorWhenDbClientIsNull() {
      assertThrows(NullPointerException.class, () ->
              new Analyzer(null,
                      new Artefact(0, "artefactTest", ".",
                              new Date(System.currentTimeMillis()), false)));
    }

    @Test
    public void shouldGetAnErrorWhenArtefactIsNull() {
      assertThrows(NullPointerException.class, () -> new Analyzer(new DbClient() {
        @Override
        public <U, T extends Subscribable<U>> T inTransaction(Function<DbTransaction, T> function) {
          return null;
        }

        @Override
        public <U, T extends Subscribable<U>> T execute(Function<DbExecute, T> function) {
          return null;
        }

        @Override
        public String dbType() {
          return null;
        }

        @Override
        public <C> Single<C> unwrap(Class<C> aClass) {
          return null;
        }
      }, null));
    }

 */
  }

}
