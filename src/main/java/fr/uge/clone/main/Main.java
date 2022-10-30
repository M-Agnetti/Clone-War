package fr.uge.clone.main;

import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        System.out.println("hello hello");

        Routing routing = Routing.builder()
                .get("/", (req, res) -> res.send("Hello World!"))
                .build();

        Config config = Config.create().get("server");
        WebServer webServer = WebServer.builder(routing)
                .config(config)
                .build();
        webServer.start()
                .await(10, TimeUnit.SECONDS);

        System.out.println("Server started at: http://localhost:" + webServer.port());
    }
}
