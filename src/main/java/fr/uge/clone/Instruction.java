package fr.uge.clone;

import java.util.Objects;

public record Instruction(int idHash, int hash, String file, int line) {

    public Instruction {
        Objects.requireNonNull(file, "file is null");
    }
}
