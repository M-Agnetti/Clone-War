package fr.uge.clone.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.clone.CloneService;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.media.multipart.MultiPartSupport;
import io.helidon.openapi.OpenAPISupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.staticcontent.StaticContentSupport;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {


        Config dbConfig = Config.create().get("db");
/*
        DbClient dbClient = DbClient.create(dbConfig);
        dbClient.execute(exec -> exec
                .namedDml("create-artefact"))
                .await();
*/

        Routing routing = Routing.builder()
                .register("/", new CloneService(DbClient.create(dbConfig)))
                .register("/", StaticContentSupport.builder("/static")
                        .welcomeFileName("index.html")
                        .build())
                .register(OpenAPISupport.create(dbConfig))
                .build();

        Config config = Config.create().get("server");
        WebServer webServer = WebServer.builder(routing)
                .config(config)
                .addMediaSupport(JacksonSupport.create(new ObjectMapper()))
                .addMediaSupport(MultiPartSupport.create())
                .build();
        webServer.start()
                .await(10, TimeUnit.SECONDS);

        System.out.println("Server started at: http://localhost:" + webServer.port());

    }
}
