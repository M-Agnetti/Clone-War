package fr.uge.clone.analyze;
import fr.uge.clone.repository.CloneRepository;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;

import java.util.List;
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


    private Map<Long, Long> groupingByIdNbClone(Map<Long, Map<Long, List<DbRow>>> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().values().stream()
                                .mapToLong(list -> list.size()==1? 1 : 1 + list.get(1).column("ID1").as(Long.class) - list.get(0).column("ID1").as(Long.class))
                                .sum()));
    }

    private Map<Long, Map<Long, List<DbRow>>> groupingByIdclone(Map<Long, List<DbRow>> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> entry.getValue().stream()
                        .collect(Collectors.groupingBy(dbRow -> dbRow.column("IDCLONE").as(Long.class),
                                Collectors.toList()))));
    }
    private void setScore(){
        var clones = repository.countClones(id);
        var tmp = clones.stream().collect(Collectors.groupingBy(dbRow -> dbRow.column("ID").as(Long.class), Collectors.toList()));
        var map = groupingByIdclone(tmp);
        var sums = groupingByIdNbClone(map);

        sums.forEach((key, value) -> {
            repository.insertScore(id, key,value);
            repository.insertScore(key, id, value);
        });
    }


    /**
     * Launches the clone detection.
     */
    public void detect() {
        var currents = repository.selectInstrOfArtifact(id);
        repository.getAllOtherInstructions(id).forEach((id, instr) -> {
            for(var i = 0 ; i < currents.size() ; i++) {
                for (var j = 0; j < instr.size(); j++) {
                    if (currents.get(i).hash() == instr.get(j).hash()) {
                        repository.insertClone(currents.get(i).idHash(), instr.get(j).idHash());
                        var cpt = 0;
                        for (i++, j++; i < currents.size() && j < instr.size(); i++, j++, cpt++) {
                            if (!currents.get(i - 1).file().equals(currents.get(i).file()) || !instr.get(j - 1).file().equals(instr.get(j).file()) || currents.get(i).hash() != instr.get(j).hash()) {
                                break;
                            }
                        }
                        if (cpt > 0) {
                            repository.insertCloneSuc(repository.getLastCloneId(), currents.get(i-- - 1).idHash(), instr.get(j - 1).idHash());
                            break;
                        }
                        break;
                    }
                }
            }
        });
        setScore();
    }
}
