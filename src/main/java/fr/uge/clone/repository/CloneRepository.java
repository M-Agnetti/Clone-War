package fr.uge.clone.repository;

import fr.uge.clone.model.*;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CloneRepository {

    private final DbClient dbClient;

    public CloneRepository(DbClient dbClient){
        Objects.requireNonNull(dbClient, "dbClient is null");
        this.dbClient = dbClient;
    }

    /**
     * Inserts a jar into the database
     * @param classes the stream representing the class jar
     * @param sources the stream representing the source jar
     */
    public void insertJar(ByteArrayInputStream classes,  ByteArrayInputStream sources){
        Objects.requireNonNull(classes);
        Objects.requireNonNull(sources);
        dbClient.execute(exec -> exec.createNamedInsert("insert-jar")
                .addParam(classes)
                .addParam(sources)
                .execute()).await();
    }

    /**
     * Inserts an artifact into the database
     * @param id the artifact's id
     * @param name the artifact's name
     * @param url the artifact's url
     */
    public void insertArtefact(long id, String name, String url){
        Objects.requireNonNull(name);
        Objects.requireNonNull(url);
        dbClient.execute(exec -> exec.createNamedInsert("insert-artefact")
                .addParam(id).addParam(name).addParam(url)
                .execute()).await();
    }

    /**
     * Inserts the metadata of an artifact into the database
     * @param id the artifact's id
     * @param groupId the artifact's groupId
     * @param artifactId the artifact's artifactId
     * @param version the artifact's version
     */
    public void insertMetadata(long id, String groupId, String artifactId, String version){
        Objects.requireNonNull(groupId);
        Objects.requireNonNull(artifactId);
        Objects.requireNonNull(version);
        dbClient.execute(exec -> exec.createNamedInsert("insert-metadata")
                .addParam(id).addParam(groupId).addParam(artifactId).addParam(version)
                .execute()).await();
    }

    /**
     * Inserts a hash of an artifact's instruction.
     *
     * @param id the artifact's id
     * @param hashValue the hash value
     * @param file the file where the instruction is
     * @param line the first line of the instruction
     */
    public void insertInstruction(long id, int hashValue, String file, int line){
        dbClient.execute(exec -> exec
                .createNamedInsert("insert-instruction")
                .addParam(hashValue)
                .addParam(file)
                .addParam(line)
                .addParam(id)
                .execute()).await();
    }

    /**
     * Inserts the first clone of a series of successive clones
     *
     * @param id1 the id of the first artifact's instruction of the first clone
     * @param id2 the id of the second artifact's instruction of the first clone
     */
    public void insertClone(long id1, long id2){
        dbClient.execute(exec -> exec.createNamedInsert("insert-clone")
                .addParam(id1)
                .addParam(id2)
                .execute()).await();
    }

    /**
     * Inserts the last part of a series of successive clones
     *
     * @param idClone the id of the first clone
     * @param id1 the id of the first artifact's instruction of the last clone
     * @param id2 the id of the second artifact's instruction of the last clone
     */
    public void insertCloneSuc(long idClone, long id1, long id2){
        dbClient.execute(exec -> exec.createNamedInsert("insert-clone-id")
                .addParam(idClone)
                .addParam(id1)
                .addParam(id2)
                .execute()).await();
    }

    /**
     * Inserts a score between 0 and 100 (a percentage) between two artifacts
     * @param id1 the first artifact's id
     * @param id2 the second artifact's id
     * @param score the score between 0 and 100
     */
    public void insertScore(long id1, long id2, long score){
        dbClient.execute(exec -> exec.createNamedInsert("insert-score")
                .addParam(id1).addParam(id2).addParam(score)
                .execute()).await();
    }

    /**
     * Selects an element for the table Jar and returns it as a Jar
     *
     * @param id the jar's id
     * @return the jar selected
     */
    public Jar selectJarById(long id){
        var jar = dbClient.execute(exec -> exec.createNamedGet("select-jar-by-id")
                .addParam("id", id)
                .execute()).await().map(dbRow -> dbRow.as(Jar.class));
        return jar.orElseThrow();
    }

    /**
     * Select the 5 higher scores for an artifact
     *
     * @param id the artifact's id
     * @return the list of its 5 higher scores
     */
    public List<Score> selectBestScores(long id){
        try {
            return dbClient.execute(dbExecute -> dbExecute
                    .createNamedQuery("select-best-scores").addParam("id", id)
                    .execute().map(dbRow -> dbRow.as(Score.class))).collectList().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Select an artifact by its id
     *
     * @param id the artifact's id
     * @return the
     */
    public Artefact selectArtById(long id){
        return dbClient.execute(exec -> exec.createNamedGet("select-art-by-id")
                .addParam("id", id).execute()).await().map(dbRow -> dbRow.as(Artefact.class)).orElseThrow();
    }

    /**
     * Selects all the lines of the table Artefact
     *
     * @return the list of all the artefact of the database
     */
    public List<Artefact> selectAllArtefacts(){
        try {
            return dbClient.execute(dbExecute -> dbExecute
                    .createNamedQuery("select-all-artefacts")
                    .execute()
                    .map(dbRow -> dbRow.as(Artefact.class))).collectList().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Selects the last jar and maps it to a Jar object
     *
     * @return the jar
     */
    public Jar selectLastJar(){
        return dbClient.execute(exec -> exec.createNamedGet("get-last-jar")
                .execute()).await().map(dbRow -> dbRow.as(Jar.class)).orElseThrow();
    }

    /**
     * Selects a line of the table Instruction by its primary key id
     * and returns it as an Instruction object
     *
     * @param id the instruction's id
     * @return the Instruction selected
     */
    public Instruction selectInstrById(long id){
        return dbClient.execute(exec -> exec.createNamedGet("select-instr-by-idHash")
                .addParam("id", id).execute())
                .await().map(dbRow -> dbRow.as(Instruction.class)).orElseThrow();
    }

    /**
     * Selects and returns all the instructions of an artifact.
     *
     * @param id the artifact's
     * @return the list of instructions
     */
    public List<Instruction> selectInstrOfArtifact(long id){
        try {
            return dbClient.execute(dbExecute -> dbExecute.createNamedQuery("select-hash-by-id")
                            .addParam("id", id).execute().map(dbRow -> dbRow.as(Instruction.class))).collectList().get()
                    .stream().sorted(Comparator.comparing(Instruction::idHash)).toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Selects all the instruction other than a specific artifact.
     *
     * @param id the artifact's id
     * @return a map which associates each artifact's id to its list of instructions.
     */
    public Map<Long, List<Instruction>> getAllOtherInstructions(long id){
        try {
            return dbClient.execute(dbExecute -> dbExecute.createNamedQuery("select-all-hash")
                    .addParam("id", id).execute().map(dbRow -> dbRow.as(Instruction.class))).collectList().get()
                    .stream().collect(Collectors.groupingBy(Instruction::id, Collectors.toList()));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Selects the metadata of an artifact
     *
     * @param id the artifact's id
     * @return an Optional containing the metadata if found, an empty Optional otherwise
     */
    public MetaData selectMetaDataById(long id){
        return dbClient.execute(exec -> exec
                .createNamedGet("select-metadata-by-id")
                .addParam("id", id)
                .execute()).await().map(dbRow -> dbRow.as(MetaData.class)).orElseThrow();
    }

    /**
     * Selects all the lines of the Clone table of an artifact
     * and maps them as Clone objects.
     *
     * @param id the artifact's id
     * @return the list of clones
     */
    public List<Clone> selectClonesOfArtifact(long id){
        try {
            return dbClient.execute(dbExecute -> dbExecute.createNamedQuery("get-clone-of-art")
                    .addParam("id1", id).addParam("id2", id).execute()
                    .map(dbRow -> dbRow.as(Clone.class))).collectList().get();

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * Selects a jar
     *
     * @param idHash the primary key of the instruction
     * @return the Jar selected
     */
    public Jar selectJarByIdHash(long idHash){
        var id = selectInstrById(idHash).id();
        return selectJarById(id);
    }

    /**
     * Selects the last id inserted in the Clone table.
     *
     * @return the id selected
     */
    public long getLastCloneId(){
        var lastClone = dbClient.execute(exec -> exec.createNamedGet("get-last-clone-id")
                .execute()).await().orElse(null);
        return lastClone == null ? 0 : lastClone.column("IDCLONE").as(Long.class);
    }


    /**
     *
     *
     * @param id the artifact's id
     * @return the list of rows selected
     */
    public List<DbRow> countClones(long id){
        try {
            return dbClient.execute(dbExecute -> dbExecute.createNamedQuery("get-clone-of-art").addParam("id1", id)
                    .addParam("id2", id).execute()).collectList().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Integer> getLinesOfClone(long idHash1, long idHash2){
        var map = new HashMap<String, Integer>();

        var list = dbClient.execute(dbExecute -> dbExecute.createNamedQuery("get-lines-of-clone").addParam("id1", idHash1)
                .addParam("id2", idHash2).execute().map(row -> row.column("LINE").as(Integer.class)))
                .collectList().await();
        map.put("min", list.stream().mapToInt(Integer::valueOf).min().orElse(1));
        map.put("max", list.stream().mapToInt(Integer::valueOf).max().orElse(1));
        return map;
    }


}
