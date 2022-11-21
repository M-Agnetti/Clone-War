package fr.uge.clone;

import io.helidon.common.http.DataChunk;
import io.helidon.dbclient.DbClient;
import io.helidon.media.multipart.ReadableBodyPart;
import io.helidon.webserver.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class CloneService implements Service {

    private final DbClient dbClient;
    private final HashMap<String, List<byte[]>> map = new HashMap<>();

    public CloneService(DbClient dbClient){
        Objects.requireNonNull(dbClient, "dbClient is null");
        this.dbClient = dbClient;
    }

    @Override
    public void update(Routing.Rules rules)  {
        rules.get("/artefacts", (serverRequest, serverResponse) -> {
                    insertArtefact2();
                    getArtefacts(serverRequest, serverResponse);
                })
                .get("/artefact/{id}", this::getArtefactById)
                .put("/index/{id}", this::startAnalyze)
                .post("/class", this::insertArtefact);
    }

    public void startAnalyze(ServerRequest request, ServerResponse response){
        var id = Integer.parseInt(request.path().param("id"));
        try {
            var artefact = dbClient.execute(exec -> exec.createNamedGet("select-artefact-by-id")
                            .addParam("id", id).execute()
                            .map(dbRow -> dbRow.get().as(Artefact.class))).get();
            System.out.println(artefact);
            new Analyzer(dbClient, artefact).launch();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
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

    private static byte[] ListToArray(List<byte[]> bytes){
        return bytes.stream().reduce((bytes1, bytes2) -> {
            var arr = new byte[bytes1.length + bytes2.length];
            var index = 0;
            for (var item: bytes1) {
                arr[index++] = item;
            }
            for (var item: bytes2) {
                arr[index++] = item;
            }
            return arr;
        }).orElse(null);
    }

    public void insertArtefact2() {
        System.out.println("insertArtefact2");
        System.out.println("map : " + map);
        if(map.size() >= 2){

            System.out.println(map.get("sources").stream().mapToInt(value -> value.length).sum());
        }
        map.clear();
        /*
        if (map.size() == 2) {
            System.out.println(map);
            var sourcesBytes = ListToArray(map.get("sources"));
            var classesBytes = ListToArray(map.get("classes"));

            dbClient.execute(exec -> exec.createNamedInsert("insert-jar")
                    .addParam(new ByteArrayInputStream(classesBytes))
                    .addParam(new ByteArrayInputStream(sourcesBytes))
                    .execute()).await();

            var res = dbClient.execute(exec -> exec.createNamedGet("get-last-jar")
                    .execute()).await().get();
            var id = res.column("IDJAR").as(Integer.class);
            var sources = res.column("SOURCES").as(Blob.class);
            var dataMap = SourcesJar.getAllData(sources);

            dbClient.execute(exec -> exec.createNamedInsert("insert-artefact")
                    .addParam(id).addParam(dataMap.get("name")).addParam(dataMap.get("url"))
                    .execute()).await();

            map.clear();
        }

         */
    }
    public void insertArtefact(ServerRequest request, ServerResponse response) {
        request.content().asStream(ReadableBodyPart.class)
                .forEach(part -> {
                    System.out.println("part name " + part.name());
                    part.content().map(DataChunk::data)
                            .flatMapIterable(Arrays::asList)
                            .map(byteBuffer -> DataChunk.create(byteBuffer))
                            .forEach(chunk -> {
                                map.put(part.name() + " reussi", null);
                                map.merge(part.name(),
                                        Arrays.asList(chunk.bytes()),
                                        (v1, v2) -> {v1.addAll(v2); return v1; });
                                chunk.release();
                            });

                });
        /*
        request.content().asStream(ReadableBodyPart.class)
                    forEach(part -> { part.content().map(DataChunk::data)
                            .flatMapIterable(Arrays::asList)
                            .map(byteBuffer -> DataChunk.create(byteBuffer))
                            .forEach(chunk -> {
                                map.merge(part.name(), chunk.bytes(), (v1, v2) -> {
                                    var array = new byte[v1.length + v2.length];
                                    System.arraycopy(v1, 0, array, 0, v1.length);
                                    System.arraycopy(v2, 0, array, v1.length, v2.length);
                                    return array;
                                });
                                chunk.release();
                            });
                    });

         */
    }


    private void getArtefactById(ServerRequest request, ServerResponse response) {
        var id = Integer.parseInt(request.path().param("id"));
        dbClient.execute(exec -> exec
                        .createNamedGet("select-artefact-by-id")
                        .addParam("id", id)
                        .execute())
                .thenAccept(maybeRow -> maybeRow
                        .ifPresentOrElse(
                                row -> response.send(List.of(row.as(Artefact.class))), () -> response.send("error")));

    }
}
