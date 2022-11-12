package fr.uge.clone;


import io.helidon.common.http.DataChunk;
import io.helidon.common.http.MediaType;
import io.helidon.common.reactive.IoMulti;
import io.helidon.dbclient.DbClient;
import io.helidon.media.multipart.ReadableBodyPart;
import io.helidon.webserver.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;

public class CloneService implements Service {

    private final DbClient dbClient;
    private final Executor executor = Executors.newFixedThreadPool(4);
    private final Storage storage;

    public CloneService(DbClient dbClient){
        Objects.requireNonNull(dbClient, "dbClient is null");
        this.dbClient = dbClient;
        storage = new Storage();
    }

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/artefacts", this::getArtefacts)
                .get("/artefact/{id}", (serverRequest, serverResponse) -> serverResponse.send("hey"))
                .post("/class", (request, res) ->
                        {
                    System.out.println("REQUETE POST");
                    uploadFile(request, res);

                    /*
                    RequestPredicate.create()
                            .accepts(MediaType.APPLICATION_OCTET_STREAM)
                            .thenApply((req, resp) -> {
                                System.out.println("YOUPI");
                                //a MODIFIER
                                var s = req.path().absolute().toString();
                                System.out.println("path : " + s);
                                //req.content().readerContext().
                                //A MODIFIER
                            })
                            .otherwise((req, resp) -> {
                                System.err.println("ERROR ERROR");
                            })
                            .accept(request, res);

                     */
                }
                );
    }

    public void insertArtefact(){
        dbClient.execute(exec -> exec
                .createNamedInsert("insert-artefact")
                .addParam("3.0.0")
                .addParam("test")
                .addParam("test")
                .execute()).await();
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


    private void uploadFile(ServerRequest request, ServerResponse response) {
        MediaType contentType = request.headers().contentType()
                .filter(MediaType.MULTIPART_FORM_DATA::test)
                .orElseThrow(() -> new BadRequestException("Invalid Content-Type"));

        request.content().asStream(ReadableBodyPart.class)
                .forEach(part ->{
                            if ("class".equals(part.name())) {
                                part.content().map(DataChunk::data)
                                        .flatMapIterable(Arrays::asList)
                                        .to(IoMulti.writeToFile(storage.create(part.filename()))
                                                .executor(executor)
                                                .build());
                            } else {
                                // when streaming unconsumed parts needs to be drained
                                part.drain();
                            }
                        }
                    )
                .onError(response::send);
    }
}
