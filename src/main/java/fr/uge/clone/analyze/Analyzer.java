package fr.uge.clone.analyze;
import fr.uge.clone.model.OpcodeEntry;
import fr.uge.clone.repository.CloneRepository;
import io.helidon.dbclient.DbClient;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;

public class Analyzer {

    private final static int WIN_SIZE = 4;
    private final Blob blob;
    private final long id;
    private final CloneRepository repository;

    public Analyzer(DbClient dbClient, Blob blob, long id){
        Objects.requireNonNull(dbClient, "dbClient is null");
        Objects.requireNonNull(blob, "blob is null");
        this.blob = blob;
        this.id = id;
        this.repository = new CloneRepository(dbClient);
    }

    public void launch() {
        try {
            AsmParser.parse(blob.getBinaryStream()).forEach((fileName, opcodes) -> {
                var sub = opcodes.subList(0, Math.min(WIN_SIZE, opcodes.size())).stream().map(OpcodeEntry::opcode).toList();
                int h = hash(sub);
                repository.insertInstruction(id, h, fileName, opcodes.get(0).line());
                for (var i = 1; i + WIN_SIZE <= opcodes.size(); i++) {
                    h = nextHash(h, opcodes.get(i - 1).opcode(), opcodes.get(i + WIN_SIZE - 1).opcode());
                    repository.insertInstruction(id, h, fileName, opcodes.get(i).line());
                }
            });
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static int hash(List<Integer> list){
        int h = 0;
        int p = 5; //clé de hachage
        for(var i = 0 ; i < list.size() ; i++){
            h += (Math.pow(p, WIN_SIZE - 1 - i) * list.get(i));
        }
        return h;
    }

    private static int nextHash(int hash, int previous, int next){
        int p = 5;
        return ( (hash - previous * (int)Math.pow(p, WIN_SIZE - 1)) * p + next);
    }

}
