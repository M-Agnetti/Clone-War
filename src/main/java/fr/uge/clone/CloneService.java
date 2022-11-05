package fr.uge.clone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.helidon.dbclient.DbClient;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;


import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class CloneService implements Service {

    private final DbClient dbClient;

    public CloneService(DbClient dbClient){
        Objects.requireNonNull(dbClient, "dbClient is null");
        this.dbClient = dbClient;
    }

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/artefacts", this::getArtefacts)
                .get("/artefact/{id}", (serverRequest, serverResponse) -> serverResponse.send("hey"))
                .post("/", (serverRequest, serverResponse) -> {
                    insertArtefact();
                    System.out.println(serverRequest.content());
                });
    }

    public void insertArtefact(){
        dbClient.execute(exec -> exec
                .createNamedInsert("insert-artefact")
                .addParam("3.0.0")
                .addParam("test")
                .addParam("test")
                .execute()).await();
    }

    public void getArtefacts(ServerRequest serverRequest, ServerResponse serverResponse){
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
}
