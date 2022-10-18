import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbExecute;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.DeleteDbFiles;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import static jakarta.json.Json.createReader;

public class DatabaseConfig {

    private static final String ARTEFACTS = "/Artefacts.json";

    public static Connection connectDb() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:./h2");
        return ds.getConnection();
    }

    public static void closeDb(Connection connection) throws SQLException {
        connection.close();
    }

    public static int insertArtefact(String groupId, String artefactId, String version) throws SQLException {
        Objects.requireNonNull(groupId);
        Objects.requireNonNull(artefactId);
        Objects.requireNonNull(version);
        var co = connectDb();
        var s = co.createStatement();
        s.execute("INSERT INTO Artefact (groupId, artefactId, version) VALUES ('" + groupId + "', " +
                "'" + artefactId + "', '" + version + "')");
        var last = s.executeQuery("SELECT * FROM Artefact ORDER BY id DESC"); /*finds the id of the last artefact inserted*/
        closeDb(co);
        return last.getInt("id");
    }

    public static int insertClone(int idHash1, int idHash2) throws SQLException {
        var co = connectDb();
        var s = co.createStatement();
        s.execute("INSERT INTO Clone(idHash1, idHash2) VALUES(" + idHash1 + ", " + idHash2 + ")");
        var last = s.executeQuery("SELECT * FROM Clone ORDER BY idClone DESC"); /*finds the id of the last clone inserted*/
        closeDb(co);
        return last.getInt("idClone");
    }

    public static int insertHash(int value, String file, int line, int id) throws SQLException {
        Objects.requireNonNull(file);
        var co = connectDb();
        var s = co.createStatement();
        s.execute("INSERT INTO Hash(hashValue, file, line, id) VALUES(" + value + ", '" + file + "'," +
                line + ", " + id + ")");
        var last = s.executeQuery("SELECT * FROM Hash ORDER BY idHash DESC"); /*finds the id of the last clone inserted*/
        closeDb(co);
        return last.getInt("idHash");
    }


    public static void initArtefact(DbClient dbClient){
        dbClient.execute(dbExecute -> dbExecute
                .namedDml("create-artefact")
                .flatMapSingle(aLong -> dbExecute.namedDml("create-hash")))
                .await();
    }

    public static void main(String... args) throws Exception {


        Config dbConfig = Config.create().get("db");

        DbClient dbClient = DbClient.builder(dbConfig).build();


    }

}
