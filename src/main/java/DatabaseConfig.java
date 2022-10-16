import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.DeleteDbFiles;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class DatabaseConfig {

    public static Connection connectDb() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:./h2");
        ds.setUser("agnetti-saidi");
        ds.setPassword("marc_soumeya");
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

    public static void main(String... args) throws Exception {
        DeleteDbFiles.execute(".", "/h2", true);

        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:./h2");
        ds.setUser("agnetti-saidi");
        ds.setPassword("marc_soumeya");

        var co = ds.getConnection();

        Statement s = co.createStatement();
        s.execute("drop table if exists Artefact cascade"); //A SUPP APRES
        s.execute("drop table if exists Hash cascade"); //A SUPP APRES
        s.execute("drop table if exists Clone cascade"); //A SUPP APRES

        s.execute("create table Artefact(id serial primary key," +
                "addDate date default now()," +
                "groupId varchar(255)," +
                "artefactID varchar(255)," +
                "version varchar(50))");
        s.execute("create table Hash (idHash serial primary key, " +
                "hashValue int, " +
                "file varchar(255), " +
                "line int, " +
                "id int, " +
                "foreign key (id) references Artefact(id))");
        s.execute("create table Clone (idClone serial primary key," +
                "idHash1 int," +
                "idHash2 int," +
                "foreign key (idHash1) references Hash(idHash)," +
                "foreign key (idHash2) references Hash(idHash))");

        /*TESTS POUR L'APPLICATION*/ /*A SUPPRIMER APRES*/

        ResultSet res = s.executeQuery("SELECT * FROM Artefact");

        while (res.next()) {
            var id = res.getInt("id");
            var groupId = res.getString("groupId");
            var artefactId = res.getString("artefactId");
            System.out.println("id : " + id + " groupId : " + groupId + " artefactId :" + artefactId);
        }


        s.close();
        co.close();
    }

}
