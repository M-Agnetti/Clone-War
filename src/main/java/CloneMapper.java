import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class CloneMapper implements DbMapper<Artefact> {

    private final static Logger LOGGER = Logger.getLogger(CloneMapper.class.getName());

    @Override
    public Artefact read(DbRow dbRow) {
        var groupId = dbRow.column("groupId");
        return new Artefact(groupId.as(String.class));
    }

    @Override
    public Map<String, ?> toNamedParameters(Artefact artefact) {
        var map = Map.of(
                "groupId", artefact.groupId() );
        return map;
    }

    @Override
    public List<?> toIndexedParameters(Artefact artefact) {
        return List.of(
                artefact.groupId()
        );
    }

}
