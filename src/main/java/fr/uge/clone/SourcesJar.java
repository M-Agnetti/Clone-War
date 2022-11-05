package fr.uge.clone;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Stream;

public class SourcesJar {

    public static void main(String[] args) throws IOException {
        try(FileSystem zip = FileSystems.newFileSystem(Paths.get("sources.jar"))) {

            Path fileInZip = zip.getPath("fr/uge/test/Main" + ".java");

            try(Stream<String> lines = Files.lines(fileInZip, StandardCharsets.UTF_8)) {
                lines.forEach(System.out::println);
            }

        }
    }
}
