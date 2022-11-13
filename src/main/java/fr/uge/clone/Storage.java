package fr.uge.clone;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import io.helidon.webserver.BadRequestException;
import io.helidon.webserver.NotFoundException;

public class Storage {

    private final Path path;

    public Storage() {
        try {
            System.out.println("creating directory....");
            path = Files.createTempDirectory(Path.of("src/main/resources/jarFiles"),"");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Path storageDir() {
        return path;
    }

    public Path create(String fname) {
        Path file = path.resolve(fname);
        if (!file.getParent().equals(path)) {
            throw new BadRequestException("Invalid file name");
        }
        try {
            Files.createFile(file);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        System.out.println("path : " + file);
        return file;
    }

}