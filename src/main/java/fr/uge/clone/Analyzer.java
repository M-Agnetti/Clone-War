package fr.uge.clone;

import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.*;

public class Analyzer {

    private final static int WIN_SIZE = 3;
    private final DbClient dbClient;
    private final Blob blob;
    private final long id;

    public static void main(String[] args) throws SQLException, IOException {
        Config dbConfig = Config.create().get("db");
        DbClient dbClient = DbClient.create(dbConfig);

        var jar = dbClient.execute(exec -> exec.createNamedGet("select-jar-by-id")
                .addParam("id", 15)
                .execute()).await().get().as(Jar.class);
        var map = AsmParser.parse(jar.classes().getBinaryStream());
        System.out.println(map);
        System.err.println("------------------------------------------------------------------------------");

        var a = new Analyzer(dbClient, jar.classes(), jar.idJar());
        System.out.println("id : " + jar.idJar() + "\n***************************************************\n\n");
        a.launch();


        /************************************************************************************************/

        jar = dbClient.execute(exec -> exec.createNamedGet("select-jar-by-id")
                .addParam("id", 9)
                .execute()).await().get().as(Jar.class);
        System.err.println("------------------------------------------------------------------------------");

        a = new Analyzer(dbClient, jar.classes(), jar.idJar());
        System.out.println("id : " + jar.idJar() + "\n***************************************************\n\n");
        a.launch();



    }

    public Analyzer(DbClient dbClient, Blob blob, long id){
        Objects.requireNonNull(dbClient, "dbClient is null");
        Objects.requireNonNull(blob, "blob is null");
        this.dbClient = dbClient;
        this.blob = blob;
        this.id = id;
    }

    public void launch() {
        System.out.println("LAUNCH");
        try {
            var map = AsmParser.parse(blob.getBinaryStream());
            map.entrySet().stream().forEach(entry -> {
                ArrayList<Map.Entry<Integer, Integer>> hashs = new ArrayList<>();

                entry.getValue().entrySet().stream().sorted(Map.Entry.comparingByKey()).map(listEntry -> {
                            List<Integer> list = listEntry.getValue().stream().map(integers -> getOpcodeHash(integers)).toList();
                            return Map.entry(listEntry.getKey(), list);
                        })
                        .forEach(listEntry -> { var line = listEntry.getKey();
                            listEntry.getValue().forEach(hash -> hashs.add(Map.entry(line, hash)));
                        });

                //analyse pour chaque fichier

                int h = hashs.subList(0, Math.min(WIN_SIZE, hashs.size())).stream().mapToInt(Map.Entry::getValue).sum();
                insertInstruction(h, entry.getKey(), hashs.get(0).getKey());
                //System.out.println("LINE1 : " + hashs.get(0).getKey() + " | " + hashs.subList(0, Math.min(WIN_SIZE, hashs.size())) +  " | hashValue : " + h);
                for(var i = 1 ; i + WIN_SIZE <= hashs.size() ; i++){
                    h = h - hashs.get(i-1).getValue() + hashs.get(i + WIN_SIZE - 1).getValue();
                    //System.out.println("line : " + hashs.get(i).getKey() + " | " + hashs.subList(i, i+WIN_SIZE) +  " | hashValue : " + h);
                    insertInstruction(h, entry.getKey(), hashs.get(i).getKey());
                }
            });
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertInstruction(int hashValue, String file, int nbLine){
        dbClient.execute(exec -> exec
                .createNamedInsert("insert-instruction")
                .addParam(hashValue)
                .addParam(file)
                .addParam(nbLine)
                .addParam(id)
                .execute()).await();
    }

    private static int getOpcodeHash(List<Integer> list){
        int h;
        int i;
        for(h = i = 0 ; i < list.size() ; i++){
            h = ((h << 1) + list.get(i));
        }
        //return h % 4391;
        return Objects.hash(list.stream().toArray());
    }

    private static int rollingHash(List<Map.Entry<Integer, Integer>> list){
        int i;
        if(list.size() == 1){
            return list.get(0).getValue();
        }

        int h = list.get(0).getValue() ^ list.get(1).getValue();
        for(i = 2 ; i < list.size() ; i++){
            h = h - list.get(i-2).getValue() + list.get(i).getValue();
        }
        return h % 4391;
    }

    private static int hash(List<Map.Entry<Integer, Integer>> list){
        int h = 0;
        int p = 7; //clé de hachage
        for(var i = 0 ; i < list.size() ; i++){
            h += (Math.pow(13, i) * list.get(i).getValue());
        }
        return h % 563;
    }
/*
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

 */
}
