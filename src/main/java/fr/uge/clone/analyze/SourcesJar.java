package fr.uge.clone.analyze;
import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SourcesJar {


    public static List<List<String>> extractLines(Blob blob, String fileName, int startLine, int endLine) {
        Objects.requireNonNull(blob);
        Objects.requireNonNull(fileName);
        try {
            var input = lookUpForFile(blob.getBinaryStream(), fileName + ".java");
            try(var reader = new BufferedReader(new InputStreamReader(input))) {
                return List.of(
                        //reader.lines().skip(Math.max(0, startLine)).limit(3).toList(),
                        reader.lines().skip(startLine - 1).limit(endLine - startLine + 1).toList()
                        //reader.lines().skip(endLine).limit(3).toList()
                );
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream lookUpForFile(InputStream input, String file) {
        try(var zip = new ZipInputStream(input)){
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                if(e.getName().endsWith(file)){
                    return new ByteArrayInputStream(zip.readAllBytes());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
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

    private static String getPomDataParent(String pom, String elem){
        Pattern groupId = Pattern.compile("<parent>.*</parent>");
        return getResultFromPattern(pom, elem, groupId);
    }

    private static String getPomData(String pom, String elem){
        Pattern groupId = Pattern.compile("^(<[^<>]+>)(<[^<>]+>)*(<[^<>/]+>[^<>]+</[^<>/]+>)*(<parent>.*</parent>)*(<[^<>/]+>[^<>]+</[^<>/]+>)*<" + elem + ">[^<>]+</" + elem + ">");
        return getResultFromPattern(pom, elem, groupId);
    }

    private static String getResultFromPattern(String pom, String elem, Pattern groupId){
        String result;
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


    private static String getUrl(String pom){
        var url = getPomData(pom, "url");
        return url != null ? url : "<not specified>";
    }

    private static String getName(String pom){
        var name = getPomData(pom, "name");
        return name != null ? name : getArtifactId(pom);
    }

    private static String getGroupId(String pom){
        var res = getPomData(pom, "groupId");
        return res == null ? getPomDataParent(pom,"groupId") : res;
    }

    private static String getVersion(String pom){
        var res = getPomData(pom, "version");
        return res == null ? getPomDataParent(pom,"version") : res;
    }

    private static String getArtifactId(String pom){
        var res = getPomData(pom, "artifactId");
        return res == null ? getPomDataParent(pom,"artifactId") : res;
    }

    public static Map<String, String> getAllData(Blob blob){
        String pom;
        var map = new HashMap<String, String>();
        try {
            pom = getStringFromPom(lookUpForFile(blob.getBinaryStream(), "pom.xml"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        map.put("name", getName(pom));
        map.put("url", getUrl(pom));
        map.put("artifactId", getArtifactId(pom));
        map.put("groupId", getGroupId(pom));
        map.put("version", getVersion(pom));
        return map;
    }

}
