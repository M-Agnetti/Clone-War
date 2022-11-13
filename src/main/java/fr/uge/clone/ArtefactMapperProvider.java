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
            DbColumn artefactId = row.column("ARTEFACTID");
            DbColumn filePath = row.column("FILEPATH");
            DbColumn addDate = row.column("ADDDATE");
            DbColumn analyzing = row.column("ANALYZING");
            return new Artefact(id.as(Integer.class), artefactId.as(String.class), filePath.as(String.class),
                    addDate.as(Date.class), analyzing.as(Boolean.class));
        }

        @Override
        public Map<String, Object> toNamedParameters(Artefact value) {
            Map<String, Object> map = new HashMap<>();
            map.put("artefactId", value.artefactId());
            map.put("filePath", value.filePath());
            return map;
        }

        @Override
        public List<Object> toIndexedParameters(Artefact value) {
            List<Object> list = new ArrayList<>();
            list.add(value.artefactId());
            list.add(value.filePath());
            return list;
        }
    }
}

