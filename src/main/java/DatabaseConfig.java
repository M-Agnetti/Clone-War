import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.DeleteDbFiles;

import java.sql.Statement;

public class DatabaseConfig {

    public static void main(String... args) throws Exception {
        DeleteDbFiles.execute("~", "test", true);

        String url = "jdbc:h2:./h2";
        String name = "agnetti-saidi";
        String pass = "marc_soumeya";

        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(url);
        ds.setUser(name);
        ds.setPassword(pass);

        var co = ds.getConnection();

        Statement s = co.createStatement();
        s.execute("drop table if exists Artefact cascade"); //A SUPP APRES
        s.execute("drop table if exists Hash cascade"); //A SUPP APRES
        s.execute("drop table if exists Clone cascade"); //A SUPP APRES

        s.execute("create table Artefact(id serial primary key)");
        s.execute("create table Hash (idHash serial primary key, " +
                "fileName varchar(255), " +
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

        s.close();
        co.close();
    }

}
