package fr.uge.clone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Stream;

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

    public static void main(String[] args) throws IOException {
        readFileFromJar("sources.jar", "fr/uge/test/Main", 6, 7);
    }
}
