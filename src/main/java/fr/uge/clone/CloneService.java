package fr.uge.clone;

import io.helidon.common.http.Http;
import io.helidon.dbclient.DbClient;
import io.helidon.webserver.*;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class CloneService implements Service {

    private final DbClient dbClient;
    private final HashMap<String, byte[]> map = new HashMap<>();

    public CloneService(DbClient dbClient){
        Objects.requireNonNull(dbClient, "dbClient is null");
        this.dbClient = dbClient;
    }

    @Override
    public void update(Routing.Rules rules)  {
        rules.get("/artefacts", this::getArtefacts)
                .get("/artefact/{id}", this::getArtefactById)
                .post("/post/{type}", this::insertArtefact)
        .post("/class/UploadComplete", this::completeInsertion);
    }


    public void getArtefacts(ServerRequest serverRequest, ServerResponse serverResponse) {
        try {
            var res = dbClient.execute(dbExecute -> dbExecute
                    .createNamedQuery("select-all-artefacts")
                    .execute()
                    .map(dbRow -> dbRow.as(Artefact.class))).collectList().get();
            serverResponse.send(res);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void completeInsertion(ServerRequest request, ServerResponse response){
        dbClient.execute(exec -> exec.createNamedInsert("insert-jar")
                .addParam(new ByteArrayInputStream(map.get("classes")))
                .addParam(new ByteArrayInputStream(map.get("sources")))
                .execute()).await();

        var lastJar = dbClient.execute(exec -> exec.createNamedGet("get-last-jar")
                .execute()).await().get();
        var id = lastJar.column("IDJAR").as(Long.class);
        var sources = lastJar.column("SOURCES").as(Blob.class);
        var classes = lastJar.column("CLASSES").as(Blob.class);
        var dataMap = SourcesJar.getAllData(sources);

        dbClient.execute(exec -> exec.createNamedInsert("insert-artefact")
                .addParam(id).addParam(dataMap.get("name")).addParam(dataMap.get("url"))
                .execute()).await();
        dbClient.execute(exec -> exec.createNamedInsert("insert-metadata")
                .addParam(id).addParam(dataMap.get("groupId")).addParam(dataMap.get("artifactId"))
                .addParam(dataMap.get("version")).execute()).await();

        map.clear();
        response.send(Http.Status.OK_200).thenAccept(__ -> new Analyzer(dbClient, classes, id).launch());

    }


    public void insertArtefact(ServerRequest request, ServerResponse response) {
        var type = request.path().param("type");

        request.content().map(byteBuffers -> byteBuffers.bytes()).collectList()
                .forSingle(bytes1 -> bytes1.forEach(bytesArray ->
                    map.merge(type, bytesArray, (v1, v2) -> {
                        var array = new byte[v1.length + v2.length];
                        System.arraycopy(v1, 0, array, 0, v1.length);
                        System.arraycopy(v2, 0, array, v1.length, v2.length);
                        return array;
                })))
                .thenAccept(unused -> response.send(Http.Status.ACCEPTED_202));
    }


    private void getArtefactById(ServerRequest request, ServerResponse response) {
        var id = Integer.parseInt(request.path().param("id"));
        dbClient.execute(exec -> exec
                        .createNamedGet("select-metadata-by-id")
                        .addParam("id", id)
                        .execute())
                .thenAccept(maybeRow -> maybeRow
                        .ifPresentOrElse(
                                row -> response.send(List.of(row.as(MetaData.class))), () -> response.send("error")));

    }
}
