package fr.uge.clone;

import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.spi.DbMapperProvider;

import java.util.*;

public class ScoreMapperProvider implements DbMapperProvider {
    private static final ScoreMapper MAPPER = new ScoreMapper();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<DbMapper<T>> mapper(Class<T> type) {
        return type.equals(Score.class) ? Optional.of((DbMapper<T>) MAPPER) : Optional.empty();
    }

    static class ScoreMapper implements DbMapper<Score> {

        @Override
        public Score read(DbRow row) {
            DbColumn id1 = row.column("ID1");
            DbColumn id2 = row.column("ID2");
            DbColumn score = row.column("SCORE");
            return new Score(id1.as(Long.class), id2.as(Long.class), score.as(Long.class));
        }

        @Override
        public Map<String, Object> toNamedParameters(Score value) {
            Map<String, Object> map = new HashMap<>();
            return map;
        }

        @Override
        public List<Object> toIndexedParameters(Score value) {
            List<Object> list = new ArrayList<>();
            return list;
        }
    }
}


