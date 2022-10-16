import io.helidon.config.Config;
import io.helidon.openapi.OpenAPISupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CloneService {

    public static Routing routing(){
        Config config = Config.create();
        return Routing.builder()
                .register(OpenAPISupport.create(config))
                .build();
    }

    public static void main(String[] args) throws UnknownHostException {
        WebServer webServer = WebServer.builder()
                .bindAddress(InetAddress.getLocalHost())
                .port(8080)
                .build();
    }
}
