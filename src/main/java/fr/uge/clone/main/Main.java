package fr.uge.clone.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.clone.Artefact;
import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.staticcontent.StaticContentSupport;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws IOException {

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

        var a = new Artefact("groupId", "artefactId");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(System.out, a);
    }
}
