package fr.uge.clone;

import io.helidon.dbclient.DbClient;

import java.io.IOException;
import java.util.Objects;

public class Analyzer {
    private final DbClient dbclient;
    private final String path;

    public Analyzer(DbClient dbClient, String path){
        Objects.requireNonNull(dbClient, "dbClient is null");
        Objects.requireNonNull(path, "path is null");
        this.dbclient = dbClient;
        this.path = path;
    }

    public void launch() {
        try {
            var map = AsmParser.parse(path);
            System.out.println("launch analysis");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void hash(){

    }
}
