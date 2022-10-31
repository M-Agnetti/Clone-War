package fr.uge.clone.main;

import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.staticcontent.StaticContentSupport;
import org.h2.jdbcx.JdbcDataSource;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        Routing routing = Routing.builder()
                .register("/", StaticContentSupport.builder("/static")
                        .welcomeFileName("index.html")
                        .build())
                .get("/", (req, res) -> res.send("Hello World!"))
                .get("/pokemon", (req, res) -> res.send("hey"))
                .build();

        Config config = Config.create().get("server");
        WebServer webServer = WebServer.builder(routing)
                .config(config)
                .build();
        webServer.start()
                .await(10, TimeUnit.SECONDS);

        System.out.println("Server started at: http://localhost:" + webServer.port());
        /***************************************************************************/
/*
        System.out.println("hello hello");

        DbClient dbClient = DbClient.create(Config.create().get("db"));

        dbClient.execute(exec -> exec
                        .namedDml("create-types"));

        dbClient.execute(exec -> exec
                        .namedInsert("insert-type", 10, "joey"));

        dbClient.execute(exec -> exec
                .namedInsert("insert-type", 13, "Tom"));
        dbClient.execute(exec -> exec
                .namedInsert("insert-type", 5, "elisa"));

        dbClient.execute(exec -> exec.namedQuery("select-all-types")
                .forEach(dbRow -> System.out.println("youpi")));

        System.out.println("Echec");

 */

    }
}
