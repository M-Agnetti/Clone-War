package fr.uge.clone;

import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.spi.DbMapperProvider;

import java.sql.Blob;
import java.util.*;

public class JarMapperProvider implements DbMapperProvider {
    private static final JarMapper MAPPER = new JarMapper();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<DbMapper<T>> mapper(Class<T> type) {
        return type.equals(Jar.class) ? Optional.of((DbMapper<T>) MAPPER) : Optional.empty();
    }

    static class JarMapper implements DbMapper<Jar> {

        @Override
        public Jar read(DbRow row) {
            DbColumn id = row.column("IDJAR");
            DbColumn classes = row.column("CLASSES");
            DbColumn sources = row.column("SOURCES");
            return new Jar(id.as(Long.class), classes.as(Blob.class), sources.as(Blob.class));
        }

        @Override
        public Map<String, Object> toNamedParameters(Jar value) {
            Map<String, Object> map = new HashMap<>();
            return map;
        }

        @Override
        public List<Object> toIndexedParameters(Jar value) {
            List<Object> list = new ArrayList<>();
            return list;
        }
    }
}

