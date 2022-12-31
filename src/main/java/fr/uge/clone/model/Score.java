package fr.uge.clone.model;

/**
 * Record representing the cloning Score
 * between two Artefacts.
 * @param id1 the first Artefact
 * @param id2 the second Artefact
 * @param score the actual score of
 *              cloning between the
 *              two id's of Artefact
 */
public record Score(long id1, long id2, long score) {
}
