package fr.uge.clone;

import java.time.LocalDate;

public record Artefact(int id, String version, String groupId, String artefactId, LocalDate addDate,
                       boolean analyzing) {
}
