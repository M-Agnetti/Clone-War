package fr.uge.clone;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import io.helidon.webserver.BadRequestException;
import io.helidon.webserver.NotFoundException;

public class Storage {

    private final Path storageDir;

    /**
     * Create a new instance.
     */
    public Storage() {
        try {
            System.out.println("creating directory....");
            storageDir = Path.of("src/main/resources/fileuploads");
            if (!Files.exists(storageDir)) {
                Files.createDirectory(storageDir);
                System.out.println("Directory created");
            }

            //storageDir = Files.createTempDirectory(Path.of("src/main/resources/"));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Path storageDir() {
        return storageDir;
    }


    public Stream<String> listFiles() {
        try {
            return Files.walk(storageDir)
                    .filter(Files::isRegularFile)
                    .map(storageDir::relativize)
                    .map(java.nio.file.Path::toString);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }


    public Path create(String fname) {
        Path file = storageDir.resolve(fname);
        if (!file.getParent().equals(storageDir)) {
            throw new BadRequestException("Invalid file name");
        }
        try {
            Files.createFile(file);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return file;
    }


    public Path lookup(String fname) {
        Path file = storageDir.resolve(fname);
        if (!file.getParent().equals(storageDir)) {
            throw new BadRequestException("Invalid file name");
        }
        if (!Files.exists(file)) {
            throw new NotFoundException("file not found");
        }
        if (!Files.isRegularFile(file)) {
            throw new BadRequestException("Not a file");
        }
        return file;
    }
}