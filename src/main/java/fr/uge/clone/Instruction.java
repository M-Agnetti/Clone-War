package fr.uge.clone;

import java.util.Objects;

public record Instruction(long idHash, int hash, String file, int line, long id) {

    public Instruction {
        Objects.requireNonNull(file, "file is null");
    }
}
