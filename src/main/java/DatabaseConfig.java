import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;

import java.sql.SQLException;
import java.util.List;

public class DatabaseConfig {

    public static int insertClone(int idHash1, int idHash2) {
        return 0;
    }

    public static int insertHash(int value, String file, int line, int id) throws SQLException {
        return 0;
    }


    public static void initArtefact(DbClient dbClient){

        dbClient.execute(dbExecute -> dbExecute
                .namedDml("create-artefact")
                .flatMapSingle(result -> dbExecute.namedDml("create-hash")))
                .await();

    }

    public static void dropTables(DbClient dbClient){

        dbClient.execute(dbExecute -> dbExecute
                        .namedDml("drop-tables"));
    }

    public static void insertArtefact(DbClient dbClient){
        dbClient.execute(dbExecute -> dbExecute
                .createNamedInsert("insert-artefact")
                .addParam("fr.uge.slice")
                .execute());
    }

    public static void main(String... args) throws Exception {

        Config dbConfig = Config.create().get("db");

        DbClient dbClient = DbClient.builder(dbConfig).build();

        //dropTables(dbClient);
        //initArtefact(dbClient);
        insertArtefact(dbClient);
        insertArtefact(dbClient);

        dbClient.execute(dbExecute -> dbExecute
                .namedQuery("select-all-artefact"))
                .map(dbRow -> dbRow.column("id"))
                .forEach(dbColumn -> System.out.println(dbColumn));

        List<Artefact> a =
                dbClient.execute(exec -> exec.namedQuery("select-all-artefact"))
                        .map(it -> it.as(Artefact.class)).collectList().get();

        System.out.println("size : " + a.size() + " " + a.toString());

        System.out.println("HELLO");

    }

}
