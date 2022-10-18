import io.helidon.common.http.Http;
import io.helidon.config.Config;
import io.helidon.media.jackson.JacksonSupport;
import io.helidon.webserver.Handler;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.staticcontent.StaticContentSupport;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class Main {

    private static Routing createRouting(Config config) {

        return null;
    }

    public static void main(String[] args) throws IOException {

        /*A REECRIRE DANS LE PROJET*/
        Config config = Config.create();

        Routing routing = Routing.builder() //IMPORTANT
                .get("/", ((serverRequest, serverResponse) -> serverResponse.send("welcome !")))
                .get("/hello", (req, res) -> {
                    // terminating logic
                    res.status(Http.Status.ACCEPTED_202);
                    res.send("Saved!");
                })
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
