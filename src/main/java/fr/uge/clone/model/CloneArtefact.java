package fr.uge.clone.model;

/**
 * Record representing a Clone Artefact.
 * @param artefact the artefact
 * @param score the score of the clone
 */
public record CloneArtefact(Artefact artefact, long score) {
}
