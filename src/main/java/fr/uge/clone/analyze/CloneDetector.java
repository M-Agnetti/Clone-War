package fr.uge.clone.analyze;

import fr.uge.clone.repository.CloneRepository;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CloneDetector {

    private final long id;
    private final CloneRepository repository;

    public CloneDetector(DbClient dbClient, long id){
        Objects.requireNonNull(dbClient, "dbClient is null");
        this.id = id;
        this.repository = new CloneRepository(dbClient);
    }


    private void setScore(){
        System.out.println("SET SCORE");
        var nbInstr = repository.countInstructions(id);
        var clones = repository.countClones(id);
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

            sums.forEach((key, value) -> {
                repository.insertScore(id, key, (value * 100) / nbInstr);
                repository.insertScore(key, id, (value * 100) / repository.countInstructions(key));
            });
    }


    public static void main(String[] args) {
        Config dbConfig = Config.create().get("db");
        DbClient dbClient = DbClient.create(dbConfig);
        new CloneDetector(dbClient, 2).setScore();
    }

    /**
     * Detect a clone between all instructions of an
     * Artifact and all other instructions and calculate
     * the score of cloning.
     */
    public void detect() {
        System.out.println("DETECT CLONE");
        var currents = repository.selectInstrOfArtifact(id);
        var instr = repository.getAllOtherInstructions(id);
        var total = 0;

        for(var i = 0 ; i < currents.size() ; i++) {
            for(var j = 0 ; j < instr.size() ; j++) {
                if(currents.get(i).hash() == instr.get(j).hash()){
                    repository.insertClone(currents.get(i).idHash(), instr.get(j).idHash());
                    var idClone = repository.getLastCloneId();
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
                        repository.insertCloneSuc(idClone, currents.get(i-- -1).idHash(), instr.get(j-1).idHash());
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
