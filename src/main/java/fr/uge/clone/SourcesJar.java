package fr.uge.clone;

import java.awt.image.ImageProducer;
import java.io.*;
import java.nio.file.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.jar.JarEntry;
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

    private static void lookUpForPom2(File jar) {
        try(var zip = new ZipFile(jar)) {
            var entries = zip.entries();
            while(entries.hasMoreElements()){
                var entry = entries.nextElement();
                System.out.println(entry.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream lookUpForPom(InputStream input) {
        try(var zip = new ZipInputStream(input)){
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                System.out.println(e.getName());
                if(e.getName().endsWith("pom.xml")){
                    return new ByteArrayInputStream(zip.readAllBytes());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }



    private static File createSourcesJar(byte[] bytes) {
        File f;
        try {
            f = File.createTempFile("tmp", ".jar", new File("target/classes/"));
            f.deleteOnExit();
            System.out.println(f.getPath());
            new FileOutputStream(f).write(bytes);
            return f;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getStringFromPom(InputStream input){
        var builder = new StringBuilder("");
        try(var reader = new BufferedReader(new InputStreamReader(input))) {
            reader.lines().forEach(s -> { builder.append(s); });
            return builder.toString().replaceAll("[ ]", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getPomData(String pom, String elem){
        String result;
        Pattern groupId = Pattern.compile("^(<[^<>]+><[^<>]+>)(<[^<>/]+>[^<>]+</[^<>/]+>)*(<parent>.*</parent>)*(<[^<>/]+>[^<>]+</[^<>/]+>)*<" + elem + ">[^<>]+</" + elem + ">");
        Pattern groupId2 = Pattern.compile("<" + elem + ">[^<>]+</" + elem + ">");

        var matcher = groupId.matcher(pom);
        while (matcher.find()) {
            var m = groupId2.matcher(matcher.group());
            m.find();
            do {
                result = m.group().replaceAll("<" + elem + ">|</" + elem + ">", "");
            } while (m.find());
            return result;
        }
        return null;
    }

    public static String getUrl(String pom){
        var url = getPomData(pom, "url");
        return url != null ? url : "<not specified>";
    }

    public static String getName(String pom){
        var name = getPomData(pom, "name");
        return name != null ? name : getPomData(pom, "artifactId");
    }

    public static String getGroupId(String jarPath){
        return null;
    }

    public static String getVersion(String jarPath){
        return null;
    }

    public static Map<String, String> getAllData(Blob blob){
        String pom;
        var map = new HashMap<String, String>();
        try {
            pom = getStringFromPom(lookUpForPom(blob.getBinaryStream()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        map.put("name", getName(pom));
        map.put("url", getUrl(pom));
        System.out.println(pom);
        return map;
    }


    public static Map<String, String> getAllData2(Blob blob){
        String pom;
        try {
            var file = createSourcesJar(blob.getBinaryStream().readAllBytes());
            //lookUpForPom(file);
            //pom = getStringFromInput();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
        var map = new HashMap<String, String>();
        /*

        map.put("name", getName(pom));
        map.put("url", getUrl(pom));

         */
        return map;
    }

}
