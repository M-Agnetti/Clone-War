import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args){

        System.out.println("hello");
        WebServer webServer = WebServer
                .create(Routing.builder()
                        .any((req, res) -> res.send("it works !!!")))
                .start()
                .await(10, TimeUnit.SECONDS);

        System.out.println("Server started at: http://localhost:" + webServer.port());
    }
}
