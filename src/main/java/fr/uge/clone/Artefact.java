package fr.uge.clone;

import java.sql.Date;

public record Artefact(int id, String version, String groupId, String artefactId, Date addDate,
                       boolean analyzing) {
}
