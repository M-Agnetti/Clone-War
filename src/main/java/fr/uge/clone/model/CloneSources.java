package fr.uge.clone.model;

import java.util.List;

public record CloneSources(Artefact artefact, List<List<List<String>>> lines) {
}
