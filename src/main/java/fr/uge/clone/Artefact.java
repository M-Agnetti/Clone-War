package fr.uge.clone;

import java.sql.Date;

public record Artefact(int id, String artefactId, String filePath, Date addDate, boolean analyzing) {
}
