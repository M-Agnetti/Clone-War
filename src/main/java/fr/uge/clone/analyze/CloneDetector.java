package fr.uge.clone.analyze;

import fr.uge.clone.model.Instruction;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CloneDetector {

    private final DbClient dbClient;
    private final long id;
    private boolean loading;

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
                    .addParam("id", id).execute().map(dbRow -> dbRow.as(Instruction.class))).collectList().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertScore(long id1, long id2, long score){
        dbClient.execute(exec -> exec.createNamedInsert("insert-score")
                .addParam(id1).addParam(id2).addParam(score)
                .execute()).await();
    }


    private long countInstructions(long id){
        return dbClient.execute(exec -> exec.createNamedGet("count-instr-by-id")
                .addParam("id", id).execute()).await().get().column("NB").as(Long.class);
    }

    private List<DbRow> countClones(long id){
        try {
            return dbClient.execute(dbExecute -> dbExecute.createNamedQuery("count-clone-by-id").addParam("id1", id)
                    .addParam("id2", id).execute()).collectList().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    private void setScore(){
        System.out.println("SET SCORE");
        var nbInstr = countInstructions(id);
        var clones = countClones(id);
            System.out.println("nombre : " + nbInstr + " | id : " + id);

            var tmp = clones.stream()
                    .collect(Collectors.groupingBy(dbRow -> dbRow.column("ID").as(Long.class), Collectors.toList()));

                    var map = tmp.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().stream()
                    .collect(Collectors.groupingBy(dbRow -> dbRow.column("IDCLONE").as(Long.class),
                            Collectors.toList()))));
            var sums = map.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().values().stream()
                           // .peek(list -> System.out.println(list.size() == 1 ? list.get(0).column("ID1").as(Long.class) :
                         //           list.get(1).column("ID1").as(Long.class) + " " + list.get(0).column("ID1").as(Long.class)))
                    .mapToLong(list -> list.size() == 1 ? 1 : 1 + list.get(1).column("ID1").as(Long.class) - list.get(0).column("ID1").as(Long.class))
                    .sum()));
            System.out.println(sums);

            sums.entrySet().forEach(entry -> {
                insertScore(id, entry.getKey(), (entry.getValue() * 100) / nbInstr);
                insertScore(entry.getKey(), id, (entry.getValue() * 100) / countInstructions(entry.getKey()));
            });
    }

    public static void main(String[] args) {
        Config dbConfig = Config.create().get("db");
        DbClient dbClient = DbClient.create(dbConfig);
        new CloneDetector(dbClient, 2).setScore();
    }

    public void detect() {
        System.out.println("DETECT CLONE");
        var currents = getCurrentInstructions();
        var instr = getAllInstructions();
        var total = 0;

        for(var i = 0 ; i < currents.size() ; i++) {
            for(var j = 0 ; j < instr.size() ; j++) {
                if(currents.get(i).hash() == instr.get(j).hash()){
                    insertClone(currents.get(i).idHash(), instr.get(j).idHash());
                    var idClone = getLastCloneId();
                    var cpt = 0;
                    for(i++, j++; i < currents.size() && j < instr.size();  i++, j++, cpt++)  {
                        if(!currents.get(i - 1).file().equals(currents.get(i).file()) || !instr.get(j - 1).file().equals(instr.get(j).file())
                        || currents.get(i).hash() != instr.get(j).hash()) {
                            break;
                        }
                    }
                    System.out.println("cpt : " + (cpt+1) + " | idClone : " + idClone);
                    total = total + cpt + 1;
                    if(cpt > 0){
                        insertCloneSucc(idClone, currents.get(i-- -1).idHash(), instr.get(j-1).idHash());
                        break;
                    }
                    break;
                }
            }
        }
        System.out.println("total : " + total);
        System.out.println("FINI");
        setScore();
    }
}
