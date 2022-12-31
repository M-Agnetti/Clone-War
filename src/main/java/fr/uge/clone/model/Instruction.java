package fr.uge.clone.model;

import java.util.Objects;

/**
 * Record representing an instruction in
 * a java file.
 * @param idHash the id of its hash
 * @param hash the value of its hash
 * @param file the name of the file where
 *             the instruction is written
 * @param line the line of the instruction
 * @param id the id of the instruction
 */
public record Instruction(long idHash, int hash, String file, int line, long id) {

    public Instruction {
        Objects.requireNonNull(file, "file is null");
    }
}
