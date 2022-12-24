package fr.uge.clone;

import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;

import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CloneDetector {

    private final DbClient dbClient;
    private final long id;

    public CloneDetector(DbClient dbClient, long id){
        Objects.requireNonNull(dbClient, "dbClient is null");
        this.dbClient = dbClient;
        this.id = id;
    }

    private List<Instruction> getCurrentInstructions(){
        try {
            return dbClient.execute(dbExecute -> dbExecute.createNamedQuery("select-hash-by-id")
                    .addParam("id", id).execute().map(dbRow -> dbRow.as(Instruction.class))).collectList().get()
                    .stream().sorted(Comparator.comparing(Instruction::idHash)).toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertClone(long id1, long id2){
        dbClient.execute(exec -> exec.createNamedInsert("insert-clone")
                .addParam(id1)
                .addParam(id2)
                .execute()).await();
    }

    private void insertCloneSucc(long idClone, long id1, long id2){
        dbClient.execute(exec -> exec.createNamedInsert("insert-clone-id")
                .addParam(idClone)
                .addParam(id1)
                .addParam(id2)
                .execute()).await();
    }

    private long getLastCloneId(){
        var lastClone = dbClient.execute(exec -> exec.createNamedGet("get-last-clone-id")
                .execute()).await().orElse(null);
        return lastClone == null ? 0 : lastClone.column("IDCLONE").as(Long.class);
    }

    private List<Instruction> getAllInstructions(){
        try {
            return dbClient.execute(dbExecute -> dbExecute.createNamedQuery("select-all-hash")
                    .addParam("id", id).execute().map(dbRow -> dbRow.as(Instruction.class))).collectList().get()
                    .stream().sorted(Comparator.comparing(Instruction::idHash)).toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void detect() {
        System.out.println("DETECT CLONE");
        var currents = getCurrentInstructions();
        var instr = getAllInstructions();

        for(var i = 0 ; i < currents.size() ; i++) {
            for(var j = 0 ; j < instr.size() ; j++) {
                if(currents.get(i).hash() == instr.get(j).hash()){
                    System.out.println(currents.get(i) + " | " + instr.get(j));
                    insertClone(currents.get(i).idHash(), instr.get(j).idHash());
                    var idClone = getLastCloneId();
                    System.out.println("\n*********************************\n\nidClone : " + idClone +
                            "\n\n**********************************************\n");
                    for(i++, j++; i < currents.size() && j < instr.size();  i++, j++)  {
                        if(!currents.get(i - 1).file().equals(currents.get(i).file()) || !instr.get(j - 1).file().equals(instr.get(j).file())) {
                            break;
                        }
                        System.out.println("SUCCESSIFS : " + currents.get(i) + " | " + instr.get(j));
                    }
                    insertCloneSucc(idClone, currents.get(i-1).idHash(), instr.get(j-1).idHash());
                    break;
                }
            }
        }
    }
}
