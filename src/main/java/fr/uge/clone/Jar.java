package fr.uge.clone;

import java.sql.Blob;

public record Jar(long idJar, Blob classes, Blob sources) {
}
