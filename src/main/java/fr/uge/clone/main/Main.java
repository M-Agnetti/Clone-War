package fr.uge.clone.main;

import fr.uge.clone.Artefact;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
/*

        Routing routing = Routing.builder()
                .register("/", StaticContentSupport.builder("/static")
                        .welcomeFileName("index.html")
                        .build())
                .get("/", (req, res) -> res.send("Hello World!"))
                .get("/artefacts", (req, res) -> res.send("[{\"groupId\":\"groupId\",\"artefactId\":\"artefactId\"}," +
                        "{\"groupId\":\"groupId2\",\"artefactId\":\"artefactId2\"}," +
                        "{\"groupId\":\"groupId\",\"artefactId\":\"artefactId\"}," +
                        "{\"groupId\":\"groupId\",\"artefactId\":\"artefactId\"}," +
                        "{\"groupId\":\"groupId\",\"artefactId\":\"artefactId\"}]"))
                .build();

        Config config = Config.create().get("server");
        WebServer webServer = WebServer.builder(routing)
                .config(config)
                .build();
        webServer.start()
                .await(10, TimeUnit.SECONDS);

        System.out.println("Server started at: http://localhost:" + webServer.port());

*/
/*
        var a = new Artefact("groupId", "artefactId");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(System.out, a);
*/
        /***********************************************************************************/

        System.out.println("heey");

        DbClient dbClient = DbClient.create(Config.create().get("db"));

      System.out.println(dbClient.dbType() + " " + dbClient.toString());
/*
        dbClient.execute(exec -> exec.namedDml("create-artefact")
                        .flatMapSingle(result -> exec.namedDml("create-instruction")))
                .await();

*/

        var res = dbClient.execute(dbExecute -> dbExecute
                        .createNamedQuery("select-all-artefacts")
                                .execute()
               // .peek(dbRow -> System.out.println(dbRow.column("VERSION").as(String.class)))
                .map(dbRow -> dbRow.as(Artefact.class))).collectList().get();

        System.out.println(res.stream().map(Artefact::toString).collect(Collectors.joining(" \n")));

        //.map(dbRow -> dbRow.as(Artefact.class)).collectList().get();
        //System.out.println("size : " + res.size());


        System.out.println("FINI2");

    }
}
