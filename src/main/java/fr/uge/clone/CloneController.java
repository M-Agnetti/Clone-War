package fr.uge.clone;

import io.helidon.dbclient.DbClient;

import java.util.Objects;

public class CloneController {

    private final CloneService service;

    public CloneController(DbClient dbClient){
        Objects.requireNonNull(dbClient);
        this.service = new CloneService(dbClient);
    }

}
