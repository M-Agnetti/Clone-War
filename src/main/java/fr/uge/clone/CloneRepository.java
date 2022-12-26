package fr.uge.clone;

import io.helidon.dbclient.DbClient;

import java.io.ByteArrayInputStream;
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
     *
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
     *
     * @param id
     * @param name
     * @param url
     */
    public void insertArtefact(long id, String name, String url){
        Objects.requireNonNull(name);
        Objects.requireNonNull(url);
        dbClient.execute(exec -> exec.createNamedInsert("insert-artefact")
                .addParam(id).addParam(name).addParam(url)
                .execute()).await();
    }

    /**
     *
     * @param id
     * @param groupId
     * @param artifactId
     * @param version
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
     *
     * @param id
     * @return
     */
    public Jar selectJarById(long id){
        return dbClient.execute(exec -> exec.createNamedGet("select-jar-by-id")
                .addParam("id", id)
                .execute()).await().get().as(Jar.class);
    }

    /**
     *
     * @param id
     * @return
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
     *
     * @param id
     * @return
     */
    public Artefact selectArtById(long id){
        return dbClient.execute(exec -> exec.createNamedGet("select-art-by-id")
                .addParam("id", id).execute()).await().get().as(Artefact.class);
    }

    /**
     *
     * @return
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
     *
     * @return
     */
    public Jar selectLastJar(){
        return dbClient.execute(exec -> exec.createNamedGet("get-last-jar")
                .execute()).await().get().as(Jar.class);
    }

    /**
     *
     * @param id
     * @return
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
    public Optional<MetaData> selectMetaDataById(long id){
        return dbClient.execute(exec -> exec
                .createNamedGet("select-metadata-by-id")
                .addParam("id", id)
                .execute()).await().map(dbRow -> dbRow.as(MetaData.class));
    }

}
