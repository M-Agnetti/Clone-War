package fr.uge.clone.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.clone.controller.CloneService;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.media.multipart.MultiPartSupport;
import io.helidon.openapi.OpenAPISupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.SocketConfiguration;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.staticcontent.StaticContentSupport;
import org.eclipse.microprofile.openapi.models.OpenAPI;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Main {

    private static Routing buildRouting(Config config){
        return Routing.builder()
                .register("/", new CloneService(DbClient.create(config)))
                .register("/", StaticContentSupport.builder("/static")
                        .welcomeFileName("index.html").build())
                .register("/home", StaticContentSupport.builder("/static")
                        .welcomeFileName("index.html").build())
                .register(OpenAPISupport.create(config))
                .build();
    }

    private static WebServer createServer(Config config, Routing routing) {
        return WebServer.builder(routing)
                .config(config)
                .addMediaSupport(JacksonSupport.create(new ObjectMapper()))
                .addMediaSupport(MultiPartSupport.create())
                .addSocket(SocketConfiguration.builder().timeout(Long.MAX_VALUE, TimeUnit.HOURS).build())
                .build();
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

/*
        Config dbConfig = Config.create().get("db");

        DbClient dbClient = DbClient.create(dbConfig);

        dbClient.execute(exec -> exec
                .namedDml("create-jar")).await();

        dbClient.execute(exec -> exec
                        .namedDml("create-artefact")).await();
        dbClient.execute(exec -> exec
                        .namedDml("create-instruction")).await();
        dbClient.execute(exec -> exec
                        .namedDml("create-metadata")).await();

        dbClient.execute(exec -> exec
                .namedDml("create-clone")).await();

        dbClient.execute(exec -> exec
                .namedDml("create-score")).await();
*/

        Routing routing = buildRouting(Config.create().get("db"));
        WebServer webServer = createServer(Config.create().get("server"), routing);
        webServer.start().await(10, TimeUnit.SECONDS);
        System.out.println("Server started at: http://localhost:" + webServer.port());

    }
}
