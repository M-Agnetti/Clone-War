package fr.uge.clone.model;

/**
 * Record representing a Clone between
 * two Artefacts.
 * @param idClone the id of the clone
 * @param id1 the first Artefact id
 * @param id2 the second Artefact id
 */
public record Clone(long idClone, long id1, long id2) {
}
