package fr.uge.clone.analyze;
import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SourcesReader {

    /**
     * Extracts some lines from a given java file from
     * a Blob. It starts at line startLine and ends at
     * line endLine.
     * @param blob the Blob containing the java file
     *             we want to extract the lines from
     * @param fileName the name of the file to extract
     *                 the lines from
     * @param startLine the line to start the extraction
     * @param endLine the line to stop the extraction
     * @return the List of all the lines extracted from
     * the founded java file in the Blob.
     */

    public static List<String> extractLines(Blob blob, String fileName, int startLine, int endLine) {
        Objects.requireNonNull(blob);
        Objects.requireNonNull(fileName);
        if(startLine > endLine) {
            var tmp = startLine;
            startLine = endLine;
            endLine = tmp;
        }
        try {
            var input = getFileContent(blob.getBinaryStream(), fileName + ".java");
            assert input != null;
            try(var reader = new BufferedReader(new InputStreamReader(input))) {
                return reader.lines().skip(startLine - 1).limit(endLine - startLine + 1).toList();
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream getFileContent(InputStream input, String file) {
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

    public static boolean isSourcesJar(InputStream input) {
        Objects.requireNonNull(input);
        try(var zip = new ZipInputStream(input)){
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                if(e.getName().endsWith("pom.xml")){
                    return true;
                }
                if(e.getName().endsWith(".class")){
                    return false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static boolean isClassesJar(InputStream input) {
        Objects.requireNonNull(input);
        try(var zip = new ZipInputStream(input)){
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                if(e.getName().endsWith(".java")){
                    return false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private static String getStringFromPom(InputStream input){
        var builder = new StringBuilder();
        try(var reader = new BufferedReader(new InputStreamReader(input))) {
            reader.lines().forEach(builder::append);
            return builder.toString().replaceAll(" ", "");
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
        String result = null;
        Pattern groupId2 = Pattern.compile("<" + elem + ">[^<>]+</" + elem + ">");
        var matcher = groupId.matcher(pom);
        if (matcher.find()) {
            var m = groupId2.matcher(matcher.group());
            if(m.find()) {
                do {
                    result = m.group().replaceAll("<" + elem + ">|</" + elem + ">", "");
                } while (m.find());
            }
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

    /**
     * Gets all the data from a given Blob by
     * analyzing its pom.xml file.
     * @param blob The Blob to get the data from
     * @return a Map representing all the datas
     * of the given Blob with its name, url,
     * artifactID, groupID and version.
     */

    public static Map<String, String> getAllData(Blob blob){
        try {
            var map = new HashMap<String, String>();
            String pom = getStringFromPom(getFileContent(blob.getBinaryStream(), "pom.xml"));
            map.put("name", getName(pom));
            map.put("url", getUrl(pom));
            map.put("artifactId", getArtifactId(pom));
            map.put("groupId", getGroupId(pom));
            map.put("version", getVersion(pom));
            return map;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
