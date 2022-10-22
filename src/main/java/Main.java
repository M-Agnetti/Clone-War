import io.helidon.common.http.Http;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.webserver.*;
import io.helidon.webserver.staticcontent.StaticContentSupport;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class Main {

    private static Routing createRouting(Config config) {

        return null;
    }

    public static void insertArtefact(DbClient dbClient, ServerRequest request, ServerResponse response){
        System.out.println("insertArtefact");

        dbClient.execute(dbExecute -> dbExecute
                .createNamedQuery("create-artefact")
                .execute()
                );

        dbClient.execute(exec -> exec
                .createNamedInsert("insert-artefact")
                        .addParam("groupId")
                                .execute());


          /*      .thenAccept(count -> response.send("inserted: " + count + " values"))
                .exceptionally(throwable -> response.send(new Throwable("ERROR ERROR")))
                )
           */


        response.send("hey");


    }

    public static void main(String[] args) throws IOException {

        DbClient dbClient = DbClient.builder(Config.create().get("db")).build();


        /*A REECRIRE DANS LE PROJET*/
        Config config = Config.create();

        Routing routing = Routing.builder() //IMPORTANT
                .get("/", ((serverRequest, serverResponse) -> serverResponse.send("welcome !")))
                .get("/hello", (req, res) -> {
                    // terminating logic
                    res.status(Http.Status.ACCEPTED_202);
                    res.send("Saved!");
                })
                .get("/test", (serverRequest, serverResponse) -> insertArtefact(dbClient, serverRequest, serverResponse))
                .get("/bye", (req, res) -> res.send("Goodbye!"))
                .register("/pictures", StaticContentSupport.create(Paths.get("./libs")))
                .post("/echo",
                        Handler.create(Person.class,
                                (req, res, person) -> res.send(person)))
                .build();

        JacksonSupport jacksonSupport = JacksonSupport.create(); //IMPORTANT

        WebServer webServer = WebServer.builder(routing)
                .config(config.get("server"))
                .addMediaSupport(jacksonSupport)
                .build();

        webServer.start()
                .await(10, TimeUnit.SECONDS);
        /*FIN DECRITURE*/

        System.out.println("Server started at: http://localhost:" + webServer.port());



    }
}
