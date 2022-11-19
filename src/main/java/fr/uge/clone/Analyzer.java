package fr.uge.clone;

import io.helidon.dbclient.DbClient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class Analyzer {
    private final DbClient dbClient;
    private final Artefact artefact;

    public Analyzer(DbClient dbClient, Artefact artefact){
        Objects.requireNonNull(dbClient, "dbClient is null");
        Objects.requireNonNull(artefact, "artefact is null");
        this.dbClient = dbClient;
        this.artefact = artefact;
    }

    public void launch() {
        try {
            var map = AsmParser.parse("test3.jar");
            System.out.println("launch analysis");
            map.forEach((fileName, mapAnalyse) -> {
                mapAnalyse.forEach((line, bytecode) -> {
                    insertInstruction(hash(bytecode.toString()), fileName, line);
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertInstruction(int hashValue, String file, int nbLine){
        dbClient.execute(exec -> exec
                .createNamedInsert("insert-instruction")
                .addParam(hashValue)
                .addParam(file)
                .addParam(nbLine)
                .addParam(artefact.id())
                .execute()).await();
    }

    private static int hash(String s){
        var key = 7;
        var m = 52631; //large prime number
        long sumHash = 0;
        for(var i = 0 ; i < s.length() ; i++){
            var n = ((int)s.charAt(i)) * (long)Math.pow(key, i);
            sumHash += n;
        }
        return (int)(Math.abs(sumHash) % m);
    }
}
