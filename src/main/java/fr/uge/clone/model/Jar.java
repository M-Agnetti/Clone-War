package fr.uge.clone.model;

import java.sql.Blob;

public record Jar(long idJar, Blob classes, Blob sources) {
}
