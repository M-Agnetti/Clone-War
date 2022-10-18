import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;

import java.util.List;
import java.util.Map;


public class CloneMapper implements DbMapper<Artefact> {

    @Override
    public Artefact read(DbRow row) {
        DbColumn groupId = row.column("groupId");
        return new Artefact(groupId.as(String.class));
    }

    @Override
    public Map<String, ?> toNamedParameters(Artefact artefact) {
        return null;
    }

    @Override
    public List<?> toIndexedParameters(Artefact artefact) {
        return null;
    }

}
