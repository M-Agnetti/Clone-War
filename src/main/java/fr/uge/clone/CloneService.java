package fr.uge.clone;

import io.helidon.common.http.Http;
import io.helidon.dbclient.DbClient;
import io.helidon.webserver.*;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


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
                .get("/clone/{id}", this::getCloneSource)
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
                .execute()).await().get().as(Jar.class);
        var dataMap = SourcesJar.getAllData(lastJar.sources());

        dbClient.execute(exec -> exec.createNamedInsert("insert-artefact")
                .addParam(lastJar.idJar()).addParam(dataMap.get("name")).addParam(dataMap.get("url"))
                .execute()).await();
        dbClient.execute(exec -> exec.createNamedInsert("insert-metadata")
                .addParam(lastJar.idJar()).addParam(dataMap.get("groupId")).addParam(dataMap.get("artifactId"))
                .addParam(dataMap.get("version")).execute()).await();

        map.clear();
        response.send(Http.Status.OK_200).onComplete(() -> {
            new Analyzer(dbClient, lastJar.classes(), lastJar.idJar()).launch();
            new CloneDetector(dbClient, lastJar.idJar()).detect();
        });

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


    private Jar getJarById(long id){
        return dbClient.execute(exec -> exec.createNamedGet("select-jar-by-id")
                .addParam("id", id)
                .execute()).await().get().as(Jar.class);
    }

    private Jar getJarByIdHash(long idHash){
        var id = dbClient.execute(exec -> exec.createNamedGet("select-art-by-idHash").addParam("id", idHash)
                .execute().map(dbRow -> dbRow.get().column("ID").as(Long.class))).await();
        return getJarById(id);
    }

    private void getCloneSource(ServerRequest request, ServerResponse response){
        var id = Integer.parseInt(request.path().param("id"));
        System.out.println("id : " + id);
        var list = new ArrayList<List<List<List<String>>>>();
        try {
            var res = dbClient.execute(dbExecute -> dbExecute.createNamedQuery("get-clone-of-art")
                    .addParam("id1", id).addParam("id2", id).execute()
                    .map(dbRow -> dbRow.as(Clone.class))).collectList().get();

            var map = res.stream().collect(Collectors.groupingBy(clone -> clone.idClone(), Collectors.toList()));
            map.entrySet().forEach(clone -> {
                var jar1 = getJarByIdHash(clone.getValue().get(0).id1());
                var jar2 = getJarByIdHash(clone.getValue().get(0).id2());

                /************************************************************************/
                var id1instr1 = dbClient.execute(exec -> exec.createNamedGet("select-art-by-idHash")
                                .addParam("id", clone.getValue().get(0).id1())
                        .execute().map(dbRow -> dbRow.get().as(Instruction.class))).await();
                var id2instr1 = dbClient.execute(exec -> exec.createNamedGet("select-art-by-idHash")
                        .addParam("id", clone.getValue().get(0).id2())
                        .execute().map(dbRow -> dbRow.get().as(Instruction.class))).await();

                var id1instr2 = clone.getValue().size() == 1 ? id1instr1 :
                        dbClient.execute(exec -> exec.createNamedGet("select-art-by-idHash")
                        .addParam("id", clone.getValue().get(1).id1())
                        .execute().map(dbRow -> dbRow.get().as(Instruction.class))).await();

                var id2instr2 = clone.getValue().size() == 1 ? id2instr1 :
                        dbClient.execute(exec -> exec.createNamedGet("select-art-by-idHash")
                                .addParam("id", clone.getValue().get(1).id2())
                                .execute().map(dbRow -> dbRow.get().as(Instruction.class))).await();


                list.add(List.of(SourcesJar.extractLines(jar1.sources(), id1instr1.file(), id1instr1.line(), id1instr2.line()),
                                SourcesJar.extractLines(jar2.sources(), id2instr1.file(), id2instr1.line(), id2instr2.line())));

            });

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        response.send(list);
    }


}
