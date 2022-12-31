package fr.uge.clone.model;

import java.sql.Blob;

/**
 * Record representing a Jar file.
 * @param idJar the id of the jar
 * @param classes a Blob containing the
 *                .class files
 * @param sources a Blob containing the
 *                .java files
 */
public record Jar(long idJar, Blob classes, Blob sources) {
}
