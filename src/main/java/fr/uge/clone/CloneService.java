package fr.uge.clone;

import io.helidon.common.http.DataChunk;
import io.helidon.common.http.MediaType;
import io.helidon.common.reactive.IoMulti;
import io.helidon.dbclient.DbClient;
import io.helidon.media.multipart.ReadableBodyPart;
import io.helidon.webserver.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class CloneService implements Service {

    private final DbClient dbClient;
    private final Executor executor = Executors.newFixedThreadPool(1);

    public CloneService(DbClient dbClient){
        Objects.requireNonNull(dbClient, "dbClient is null");
        this.dbClient = dbClient;
    }

    @Override
    public void update(Routing.Rules rules)  {
        rules.get("/artefacts", this::getArtefacts)
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

    public void insertArtefact(ServerRequest request, ServerResponse response) {
        request.headers().contentType()
                .filter(MediaType.MULTIPART_FORM_DATA)
                .orElseThrow(() -> new BadRequestException("Invalid Content-Type"));

        var path = createDir();
        request.content().asStream(ReadableBodyPart.class)
                .forEach(part -> {
                                var fileName = "sources".equals(part.name()) ? "sources.jar" : "classes.jar";
                                part.content().map(DataChunk::data)
                                        .flatMapIterable(Arrays::asList)
                                        .to(IoMulti.writeToFile(createFile(path, fileName))
                                                .executor(executor)
                                                .build()
                                        );
                        }
                )
                .onError(response::send);

        dbClient.execute(exec -> exec
                .createNamedInsert("insert-artefact")
                .addParam("artefact")
                .addParam(path.toString())
                .execute()).await();
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
