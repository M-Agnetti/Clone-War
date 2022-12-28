package fr.uge.clone.repository;

import fr.uge.clone.model.*;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

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
     *
     * @param id1
     * @param id2
     * @param score
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
                .addParam("id", id).execute()
                .map(dbRow -> dbRow.get().as(Instruction.class))).await();
    }

    /**
     *
     * @param id
     * @return
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

    public List<Instruction> getAllOtherInstructions(long id){
        try {
            return dbClient.execute(dbExecute -> dbExecute.createNamedQuery("select-all-hash")
                    .addParam("id", id).execute().map(dbRow -> dbRow.as(Instruction.class))).collectList().get();
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
    public Optional<MetaData> selectMetaDataById(long id){
        return dbClient.execute(exec -> exec
                .createNamedGet("select-metadata-by-id")
                .addParam("id", id)
                .execute()).await().map(dbRow -> dbRow.as(MetaData.class));
    }

    /**
     *
     * @param id
     * @return
     */
    public List<DbRow> countClonesOfArtifact(long id){
        try {
            return dbClient.execute(dbExecute -> dbExecute.createNamedQuery("count-clone-by-id").addParam("id1", id)
                    .addParam("id2", id).execute()).collectList().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Counts the line of the table Instruction linked to a specific artifact
     *
     * @param id the artifact's id
     * @return the number of instructions of the artifact
     */
    private long countInstructionsOfArtifact(long id){
        return dbClient.execute(exec -> exec.createNamedGet("count-instr-by-id")
                .addParam("id", id).execute()).await().get().column("NB").as(Long.class);
    }

    /**
     * Selects
     *
     * @param idHash the primary key of the instruction
     * @return the Jar selected
     */
    public Jar selectJarByIdHash(long idHash){
        var id = selectInstrById(idHash).id();
        return selectJarById(id);
    }

    /**
     *
     * @return
     */
    public long getLastCloneId(){
        var lastClone = dbClient.execute(exec -> exec.createNamedGet("get-last-clone-id")
                .execute()).await().orElse(null);
        return lastClone == null ? 0 : lastClone.column("IDCLONE").as(Long.class);
    }

    /**
     *
     * @param id
     * @return
     */
    public long countInstructions(long id){
        return dbClient.execute(exec -> exec.createNamedGet("count-instr-by-id")
                .addParam("id", id).execute()).await().get().column("NB").as(Long.class);
    }

    /**
     *
     * @param id
     * @return
     */
    public List<DbRow> countClones(long id){
        try {
            return dbClient.execute(dbExecute -> dbExecute.createNamedQuery("count-clone-by-id").addParam("id1", id)
                    .addParam("id2", id).execute()).collectList().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


}
