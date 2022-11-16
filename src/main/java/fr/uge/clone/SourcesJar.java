package fr.uge.clone;

import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.*;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

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

    public static String lookUpForPom(String jarPath) {
        try(ZipFile zip = new ZipFile(jarPath)){
            var e = zip.entries();
            while (e.hasMoreElements()) {
                var entry = e.nextElement();
                if (entry.getName().endsWith("pom.xml")) {
                    return entry.getName();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static String getPomData(String path, String pomPath, String elem){
        Pattern groupId = Pattern.compile("^(<[^<>]+>(<[^<>]+>[^<>]+</[^<>]+>|[^<>]+)+)(<" + elem + ">[^<>]+</" + elem + ">)");
        Pattern groupId2 = Pattern.compile("<" + elem + ">[^<>]+</" + elem + ">");
        try(FileSystem zip = FileSystems.newFileSystem(Paths.get(path))) {
            Path fileInZip = zip.getPath(pomPath);
            if(path == null){
                return "<not specified>";
            }
            var joiner = new StringJoiner("");
            try(var reader = Files.newBufferedReader(fileInZip)){
                reader.lines().forEach(s -> { joiner.add(s); });
            }
            var matcher = groupId.matcher(joiner.toString().replaceAll("[ ]", ""));
            matcher.find();
            var m = groupId2.matcher(matcher.group());
            m.find();
            return m.group().replaceAll("(<" + elem + ">|</" + elem + ">)", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPomData2(String path, String pomPath, String elem){
        Pattern test = Pattern.compile("<[^<>/]+>[^<>]+</[^<>/]+>");
        Pattern groupId = Pattern.compile("^(<[^<>]+><[^<>]+>(<[^<>]+>[^<>]+</[^<>]+>|[^<>]+)+)(<" + elem + ">[^<>]+</" + elem + ">)");
        Pattern groupId2 = Pattern.compile("<" + elem + ">[^<>]+</" + elem + ">");
        try(FileSystem zip = FileSystems.newFileSystem(Paths.get(path))) {
            Path fileInZip = zip.getPath(pomPath);
            if(path == null){
                return "<not specified>";
            }
            var joiner = new StringJoiner("");
            try(var reader = Files.newBufferedReader(fileInZip)){
                reader.lines().forEach(s -> { joiner.add(s); });
            }
            
            /******************************************************************************/
            var s = joiner.toString().replaceAll("[ ]", "");
            var matcher = test.matcher(s);

            while(matcher.find()){
                System.err.println(s.substring(matcher.start(), matcher.end()));
                System.out.println(matcher.group());
            }
            var m = groupId2.matcher(matcher.group());
            m.find();
            return m.group().replaceAll("(<" + elem + ">|</" + elem + ">)", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        //readFileFromJar("sources.jar", "fr/uge/test/Main", 6, 7);
        var jarPath = "test/jackson-sources.jar";
        var path = lookUpForPom(jarPath);
        System.out.println("artifactId : " + getPomData2(jarPath, path, "artifactId"));
        System.out.println("groupId : " + getPomData2(jarPath, path, "groupId"));
        System.out.println("version : " + getPomData2(jarPath, path, "version"));

    }
}
