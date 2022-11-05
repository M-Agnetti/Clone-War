package fr.uge.clone.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.clone.Artefact;
import fr.uge.clone.CloneService;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.staticcontent.StaticContentSupport;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        Routing routing = Routing.builder()
                .register("/", new CloneService(DbClient.create(Config.create().get("db"))))
                .register("/", StaticContentSupport.builder("/static")
                        .welcomeFileName("index.html")

                        .build())
                .build();

        Config config = Config.create().get("server");
        WebServer webServer = WebServer.builder(routing)
                .config(config)
                .addMediaSupport(JacksonSupport.create(new ObjectMapper()))
                .build();
        webServer.start()
                .await(10, TimeUnit.SECONDS);

        System.out.println("Server started at: http://localhost:" + webServer.port());

    }
}
