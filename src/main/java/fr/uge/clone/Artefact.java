package fr.uge.clone;

import java.sql.Date;

public record Artefact(int id, String name, Date dateAdd, int analyzing, String url) {

}
