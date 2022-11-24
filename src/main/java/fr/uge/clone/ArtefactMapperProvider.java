package fr.uge.clone;

import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.spi.DbMapperProvider;

import java.sql.Date;
import java.util.*;

public class ArtefactMapperProvider implements DbMapperProvider {
    private static final ArtefactMapper MAPPER = new ArtefactMapper();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<DbMapper<T>> mapper(Class<T> type) {
        return type.equals(Artefact.class) ? Optional.of((DbMapper<T>) MAPPER) : Optional.empty();
    }

    static class ArtefactMapper implements DbMapper<Artefact> {

        @Override
        public Artefact read(DbRow row) {
            DbColumn id = row.column("ID");
            DbColumn name = row.column("NAME");
            DbColumn addDate = row.column("DATEADD");
            DbColumn analyzing = row.column("ANALYZING");
            DbColumn url = row.column("URL");
            return new Artefact(id.as(Long.class), name.as(String.class), addDate.as(Date.class), analyzing.as(Integer.class),
                    url.as(String.class));
        }

        @Override
        public Map<String, Object> toNamedParameters(Artefact value) {
            Map<String, Object> map = new HashMap<>();
            map.put("ID", value.id());
            map.put("NAME", value.name());
            map.put("URL", value.url());
            return map;
        }

        @Override
        public List<Object> toIndexedParameters(Artefact value) {
            List<Object> list = new ArrayList<>();
            list.add(value.id());
            list.add(value.name());
            list.add(value.url());
            return list;
        }
    }
}

