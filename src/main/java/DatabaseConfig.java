import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.DeleteDbFiles;

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
    }

}
