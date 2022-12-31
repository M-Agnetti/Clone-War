package fr.uge.clone.model;

import java.sql.Date;

/**
 * Record representing an Artefact.
 * @param id the id of the Artefact
 * @param name the name of the Artefact
 * @param dateAdd the adding Date of the
 *                Artefact
 * @param analyzing the analyzing status of
 *                  the Artefact
 * @param url the url of the Artefact
 */
public record Artefact(long id, String name, Date dateAdd, int analyzing, String url) {

}
