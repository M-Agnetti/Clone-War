package fr.uge.clone;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.nio.file.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class SourcesJar {

    public static void readFileFromJar(String jarPath, String filePath, int startLine, int endLine) throws IOException {
        try(FileSystem zip = FileSystems.newFileSystem(Paths.get(jarPath))) {
            Path fileInZip = zip.getPath(filePath + ".java");
            try(var reader = Files.newBufferedReader(fileInZip) ;
                var lineNumberReader = new LineNumberReader(reader)){
                    String s;
                    while((s = lineNumberReader.readLine()) != null){
                        if(lineNumberReader.getLineNumber() >= 6 && lineNumberReader.getLineNumber() <= endLine){
                            System.out.println(s);
                        }
                    }
            }
        }
    }

    private static List<ZipEntry> lookUpForPom(InputStream input) {
        List<ZipEntry> entries = new ArrayList<>();
        try(var zip = new ZipInputStream(input)){
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                entries.add(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return entries;
    }

    private static String getPomData(String path, String pomPath, String elem){
        if(path == null || pomPath == null){
            return "<not specified>";
        }
        String result;
        Pattern groupId = Pattern.compile("^(<[^<>]+><[^<>]+>)(<[^<>/]+>[^<>]+</[^<>/]+>)*(<parent>.*</parent>)*(<[^<>/]+>[^<>]+</[^<>/]+>)*<" + elem + ">[^<>]+</" + elem + ">");
        Pattern groupId2 = Pattern.compile("<" + elem + ">[^<>]+</" + elem + ">");

        try(FileSystem zip = FileSystems.newFileSystem(Paths.get(path))) {
            Path fileInZip = zip.getPath(pomPath);
            var joiner = new StringJoiner("");
            try(var reader = Files.newBufferedReader(fileInZip)){
                reader.lines().forEach(s -> { joiner.add(s); });
            }
            
            var s = joiner.toString().replaceAll("[ ]", "");
            var matcher = groupId.matcher(s);
            while(matcher.find()){
                var m = groupId2.matcher(matcher.group());
                m.find();
                do {
                    result = m.group().replaceAll("<" + elem + ">|</" + elem + ">", "");
                } while(m.find());
               return result;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "<not specified>";
    }

    public static String getUrl(String jarPath){
        return null;
    }

    public static String getArtifactId(String jarPath){
        return null;
    }

    public static String getGroupId(String jarPath){
        return null;
    }

    public static String getVersion(String jarPath){
        return null;
    }

    public static void getName(Blob blob){
        InputStream input = null;
        try {
            input = blob.getBinaryStream();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("heeey");
        var list = lookUpForPom(input);
        for(var entry:list){
            System.out.println(entry.getName());
        }
        System.out.println("finiii");
    }

    public static void main(String[] args) throws IOException {
        //readFileFromJar("sources.jar", "fr/uge/test/Main", 6, 7);
        var jarPath = "test/jackson-sources.jar";
        /*
        System.out.println("name : " + getPomData(jarPath, path, "name"));
        System.out.println("artifactId : " + getPomData(jarPath, path, "artifactId"));
        System.out.println("groupId : " + getPomData(jarPath, path, "groupId"));
        System.out.println("version : " + getPomData(jarPath, path, "version"));
        System.out.println("url : " + getUrl(jarPath));

         */

    }
}
