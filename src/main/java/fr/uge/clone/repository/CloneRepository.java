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
     * Selects an element for the table Jar and returns it as a Jar
     * @param id the jar's id
     * @return the jar selected
     */
    public Jar selectJarById(long id){
        return dbClient.execute(exec -> exec.createNamedGet("select-jar-by-id")
                .addParam("id", id)
                .execute()).await().get().as(Jar.class);
    }

    /**
     * Select the 5 higher scores for an artifact
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
     * @param id the artifact's id
     * @return the
     */
    public Artefact selectArtById(long id){
        return dbClient.execute(exec -> exec.createNamedGet("select-art-by-id")
                .addParam("id", id).execute()).await().get().as(Artefact.class);
    }

    /**
     * Selects all the lines of the table Artefact
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
     * @return the jar
     */
    public Jar selectLastJar(){
        return dbClient.execute(exec -> exec.createNamedGet("get-last-jar")
                .execute()).await().get().as(Jar.class);
    }

    /**
     * Selects a line of the table Instruction by its primary key id
     * and returns it as an Instruction object
     * @param id the instruction's id
     * @return the Instruction
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
            return dbClient.execute(dbExecute -> dbExecute.createNamedQuery("select-all-hash")
                            .addParam("id", id).execute().map(dbRow -> dbRow.as(Instruction.class))).collectList().get()
                    .stream().sorted(Comparator.comparing(Instruction::idHash)).toList();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param id
     * @return
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
     * @param id the artifact's id
     * @return the number of instructions of the artifact
     */
    private long countInstructionsOfArtifact(long id){
        return dbClient.execute(exec -> exec.createNamedGet("count-instr-by-id")
                .addParam("id", id).execute()).await().get().column("NB").as(Long.class);
    }

}
