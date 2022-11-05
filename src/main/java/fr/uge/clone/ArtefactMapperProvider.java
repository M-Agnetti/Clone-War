package fr.uge.clone;

import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.spi.DbMapperProvider;

import java.sql.Date;
import java.time.LocalDate;
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
            DbColumn groupId = row.column("GROUPID");
            DbColumn artefactId = row.column("ARTEFACTID");
            DbColumn version = row.column("VERSION");
            DbColumn addDate = row.column("ADDDATE");
            DbColumn analyzing = row.column("ANALYZING");
            return new Artefact(id.as(Integer.class), version.as(String.class), groupId.as(String.class),
                    artefactId.as(String.class), addDate.as(Date.class), analyzing.as(Boolean.class));
            //return new Artefact(1, "", "groupId.as(String.class)",
               //     "artefactId.as(String.class)", LocalDate.now(), true);
        }

        @Override
        public Map<String, Object> toNamedParameters(Artefact value) {
            Map<String, Object> map = new HashMap<>();
            map.put("groupId", value.groupId());
            map.put("artefactId", value.artefactId());
            map.put("version", value.version());
            return map;
        }

        @Override
        public List<Object> toIndexedParameters(Artefact value) {
            List<Object> list = new ArrayList<>();
            list.add(value.groupId());
            list.add(value.artefactId());
            list.add(value.version());
            return list;
        }
    }
}

