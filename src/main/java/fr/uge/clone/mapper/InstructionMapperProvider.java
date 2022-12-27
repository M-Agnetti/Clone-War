package fr.uge.clone.mapper;

import fr.uge.clone.model.Instruction;
import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;
import io.helidon.dbclient.spi.DbMapperProvider;

import java.util.*;

public class InstructionMapperProvider implements DbMapperProvider {
    private static final InstructionMapper MAPPER = new InstructionMapper();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<DbMapper<T>> mapper(Class<T> type) {
        return type.equals(Instruction.class) ? Optional.of((DbMapper<T>) MAPPER) : Optional.empty();
    }

    static class InstructionMapper implements DbMapper<Instruction> {

        @Override
        public Instruction read(DbRow row) {
            DbColumn id = row.column("IDHASH");
            DbColumn hash = row.column("HASHVALUE");
            DbColumn file = row.column("FILE");
            DbColumn line = row.column("LINE");
            DbColumn idArt = row.column("ID");
            return new Instruction(id.as(Long.class), hash.as(Integer.class), file.as(String.class), line.as(Integer.class),
                    idArt.as(Long.class));
        }

        @Override
        public Map<String, Object> toNamedParameters(Instruction value) {
            Map<String, Object> map = new HashMap<>();
            return map;
        }

        @Override
        public List<Object> toIndexedParameters(Instruction value) {
            List<Object> list = new ArrayList<>();
            return list;
        }
    }
}

