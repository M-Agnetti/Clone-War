package fr.uge.clone.mapper;

import fr.uge.clone.model.Clone;
import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.spi.DbMapperProvider;

import java.util.*;

public class CloneMapperProvider implements DbMapperProvider {
    private static final CloneMapper MAPPER = new CloneMapper();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<DbMapper<T>> mapper(Class<T> type) {
        return type.equals(Clone.class) ? Optional.of((DbMapper<T>) MAPPER) : Optional.empty();
    }

    static class CloneMapper implements DbMapper<Clone> {

        @Override
        public Clone read(DbRow row) {
            DbColumn idClone = row.column("IDCLONE");
            DbColumn id1 = row.column("ID1");
            DbColumn id2 = row.column("ID2");
            return new Clone(idClone.as(Long.class), id1.as(Long.class), id2.as(Long.class));
        }

        @Override
        public Map<String, Object> toNamedParameters(Clone value) {
            Map<String, Object> map = new HashMap<>();
            return map;
        }

        @Override
        public List<Object> toIndexedParameters(Clone value) {
            List<Object> list = new ArrayList<>();
            return list;
        }
    }
}

