package fr.uge.clone;

import io.helidon.common.http.DataChunk;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import io.helidon.media.multipart.ReadableBodyPart;
import io.helidon.webserver.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipFile;

public class CloneService implements Service {

    private final DbClient dbClient;
    private final HashMap<String, byte[]> map = new HashMap<>();

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

    public void insertArtefact2() {
        if(map.size() == 2){
            System.out.println(map);
            dbClient.execute(exec -> exec.createNamedInsert("insert-jar")
                    .addParam(new ByteArrayInputStream(map.get("classes")))
                    .addParam(new ByteArrayInputStream(map.get("sources")))
                    .execute()).await();
            var res = dbClient.execute(exec -> exec.createNamedGet("get-last-jar")
                            .execute()).await().get();
            var id = res.column("IDJAR").as(Integer.class);
            dbClient.execute(exec -> exec.createNamedInsert("insert-artefact")
                    .addParam(id).addParam("name").addParam("url")
                    .execute()).await();
            var sources = res.column("SOURCES").as(Blob.class);
            SourcesJar.getName(sources);
            map.clear();
        }
    }
    public void insertArtefact(ServerRequest request, ServerResponse response) {
         request.content().asStream(ReadableBodyPart.class)
                    .forEach(part -> {
                        var ok = part.name().equals("classes") || part.name().equals(("sources"));
                        part.content().map(DataChunk::data)
                                .flatMapIterable(Arrays::asList)
                                .map(byteBuffer -> DataChunk.create(byteBuffer))

                                .collect(HashMap<String, byte[]>::new, (mapChunk, chunk) -> {
                                    mapChunk.merge(part.name(), chunk.bytes(), (v1, v2) -> {
                                        var array = new byte[v1.length + v2.length];
                                        System.arraycopy(v1, 0, array, 0, v1.length);
                                        System.arraycopy(v2, 0, array, v1.length, v2.length);
                                        return array;
                                    });
                                    chunk.release();
                                })
                                .forSingle(byteMap -> {
                                    map.putAll(byteMap);
                                })
                                .exceptionally(response::send);
                    });
    }

    private Path createDir() {
        try {
            System.out.println("creating directory....");
            return Files.createTempDirectory(Path.of("src/main/resources/jarFiles"),"");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Path createFile(Path path, String name) {
        Path file = path.resolve(name);
        if (!file.getParent().equals(path)) {
            throw new BadRequestException("Invalid file name");
        }
        try {
            Files.createFile(file);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return file;
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
