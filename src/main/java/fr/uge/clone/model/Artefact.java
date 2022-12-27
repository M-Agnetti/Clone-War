package fr.uge.clone.model;

import java.sql.Date;

public record Artefact(long id, String name, Date dateAdd, int analyzing, String url) {

}
