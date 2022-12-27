package fr.uge.clone.analyze;

import io.helidon.common.reactive.Single;
import io.helidon.common.reactive.Subscribable;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbExecute;
import io.helidon.dbclient.DbTransaction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AnalyzerTest {
  @Nested
  public class Construction {
    @Test
    public void shouldGetAnErrorWhenDbClientIsNull() {
      assertThrows(NullPointerException.class, () ->
              new Analyzer(null, new Blob() {
                @Override
                public long length() throws SQLException {
                  return 0;
                }

                @Override
                public byte[] getBytes(long pos, int length) throws SQLException {
                  return new byte[0];
                }

                @Override
                public InputStream getBinaryStream() throws SQLException {
                  return null;
                }

                @Override
                public long position(byte[] pattern, long start) throws SQLException {
                  return 0;
                }

                @Override
                public long position(Blob pattern, long start) throws SQLException {
                  return 0;
                }

                @Override
                public int setBytes(long pos, byte[] bytes) throws SQLException {
                  return 0;
                }

                @Override
                public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
                  return 0;
                }

                @Override
                public OutputStream setBinaryStream(long pos) throws SQLException {
                  return null;
                }

                @Override
                public void truncate(long len) throws SQLException {

                }

                @Override
                public void free() throws SQLException {

                }

                @Override
                public InputStream getBinaryStream(long pos, long length) throws SQLException {
                  return null;
                }
              }, 0));
    }

    @Test
    public void shouldGetAnErrorWhenBlobIsNull() {
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
      }, null, 0));
    }
  }

}
