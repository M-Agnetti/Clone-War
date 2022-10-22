import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.spi.DbMapperProvider;
import jakarta.annotation.Priority;

import java.util.Optional;

@Priority(1000)
public class CloneMapperProvider implements DbMapperProvider {
    private static final CloneMapper MAPPER = new CloneMapper();

    @Override
    public <T> Optional<DbMapper<T>> mapper(Class<T> type) {
        if (type.equals(Artefact.class)) {
            return Optional.of((DbMapper<T>) MAPPER);
        }
        return Optional.empty();
    }
}