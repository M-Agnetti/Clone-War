package fr.uge.clone.mapper;

import fr.uge.clone.model.MetaData;
import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.spi.DbMapperProvider;
import java.util.*;

public class MetaDataMapperProvider implements DbMapperProvider {
    private static final MetaDataMapper MAPPER = new MetaDataMapper();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<DbMapper<T>> mapper(Class<T> type) {
        return type.equals(MetaData.class) ? Optional.of((DbMapper<T>) MAPPER) : Optional.empty();
    }

    static class MetaDataMapper implements DbMapper<MetaData> {

        @Override
        public MetaData read(DbRow row) {
            DbColumn idMeta = row.column("IDMETA");
            DbColumn groupId = row.column("GROUPID");
            DbColumn artifactId = row.column("ARTIFACTID");
            DbColumn version = row.column("VERSION");
            return new MetaData(idMeta.as(Long.class), groupId.as(String.class), artifactId.as(String.class), version.as(String.class));
        }

        @Override
        public Map<String, Object> toNamedParameters(MetaData value) {
            Map<String, Object> map = new HashMap<>();
            map.put("IDMETA", value.idMeta());
            map.put("GROUPID", value.groupId());
            map.put("ARTIFACTID", value.artifactId());
            map.put("VERSION", value.version());
            return map;
        }

        @Override
        public List<Object> toIndexedParameters(MetaData value) {
            List<Object> list = new ArrayList<>();
            list.add(value.idMeta());
            list.add(value.groupId());
            list.add(value.artifactId());
            list.add(value.version());
            return list;
        }
    }
}

