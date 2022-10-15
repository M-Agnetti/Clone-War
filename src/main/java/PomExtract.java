import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class PomExtract {

    public static String extractFromPom(Path path) throws IOException {
        var pattern = Pattern.compile("<groupId>[a-zA-Z0-9\\.\\-\\_]+</groupId>");
        String line;
        try(var reader = Files.newBufferedReader(path)){
            while((line = reader.readLine()) != null){
                var m = pattern.matcher(line);
                if (m.find()) {
                    var s =  m.group().replaceAll("<groupId>", "").replaceAll("</groupId>", "");
                    return s;
                }
            }
        }
        return null;
    }
}
