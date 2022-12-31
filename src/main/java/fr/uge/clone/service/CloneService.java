package fr.uge.clone.service;

import fr.uge.clone.analyze.SourcesReader;
import fr.uge.clone.model.*;
import fr.uge.clone.repository.CloneRepository;
import fr.uge.clone.analyze.Analyzer;
import fr.uge.clone.analyze.CloneDetector;
import io.helidon.common.http.DataChunk;
import io.helidon.common.http.Http;
import io.helidon.dbclient.DbClient;
import io.helidon.webserver.*;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


public class CloneService implements Service {

    private final DbClient dbClient;
    private final CloneRepository repository;
    private final HashMap<String, byte[]> map = new HashMap<>();

    public CloneService(DbClient dbClient){
        Objects.requireNonNull(dbClient, "dbClient is null");
        this.dbClient = dbClient;
        this.repository = new CloneRepository(dbClient);
    }

    /**
     * Defines all the routes and handlers for each request.
     *
     * @param rules the rules of the APIs routing.
     */
    @Override
    public void update(Routing.Rules rules)  {
        Objects.requireNonNull(rules);
        rules.get("/artefacts", this::getArtefacts)
                .get("/artefact/{id}", this::getArtefactById)
                .get("/clones/{id}", this::getCloneSource)
                .get("/scores/{id}", this::getBestScoresById)
                .get("/all-scores", this::getAllBestScores)
                .post("/post/{type}", this::insertArtefact)
                .post("/class/UploadComplete", this::completeInsertion);
    }


    /**
     *
     * @param serverRequest the server request
     * @param serverResponse the server response
     */
    private void getArtefacts(ServerRequest serverRequest, ServerResponse serverResponse){
        serverResponse.send(repository.selectAllArtefacts());
    }

    private boolean checkJars(Blob classes, Blob sources) {
        try {
            return SourcesReader.isSourcesJar(sources.getBinaryStream()) && SourcesReader.isClassesJar(classes.getBinaryStream());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     *
     * @param request the server request
     * @param response the server response
     */
    private void completeInsertion(ServerRequest request, ServerResponse response){
        repository.insertJar(new ByteArrayInputStream(map.get("classes")), new ByteArrayInputStream(map.get("sources")));
        map.clear();
        var lastJar = repository.selectLastJar();

        if(checkJars(lastJar.classes(), lastJar.sources())){
            finishAcceptedInsertion(lastJar);
            response.send(Http.Status.OK_200);
        }
        else {
            response.send(Http.Status.NOT_ACCEPTABLE_406);
        }
    }

    private void finishAcceptedInsertion(Jar jar) {
        var dataMap = SourcesReader.getAllData(jar.sources());
        repository.insertArtefact(jar.idJar(), dataMap.get("name"), dataMap.get("url"));
        repository.insertMetadata(jar.idJar(), dataMap.get("groupId"), dataMap.get("artifactId"), dataMap.get("version"));

        new Analyzer(dbClient, jar.classes(), jar.idJar()).launch();
        new CloneDetector(dbClient, jar.idJar()).detect();
    }

    /**
     *
     * @param request the server request
     * @param response the server response
     */
    private void insertArtefact(ServerRequest request, ServerResponse response) {
        var type = request.path().param("type");

        request.content().map(DataChunk::bytes).collectList()
                .forSingle(bytes1 -> bytes1.forEach(bytesArray ->
                    map.merge(type, bytesArray, (v1, v2) -> {
                        var array = new byte[v1.length + v2.length];
                        System.arraycopy(v1, 0, array, 0, v1.length);
                        System.arraycopy(v2, 0, array, v1.length, v2.length);
                        return array;
                })))
                .thenAccept(unused -> response.send(Http.Status.ACCEPTED_202));
    }


    /**
     *
     * @param request the server request
     * @param response the server response
     */
    private void getArtefactById(ServerRequest request, ServerResponse response) {
        var id = Integer.parseInt(request.path().param("id"));
        var metadata = repository.selectMetaDataById(id);
        response.send(List.of(metadata));
    }

    /**
     *
     * @param request the server request
     * @param response the server response
     */
    private void getAllBestScores(ServerRequest request, ServerResponse response) {
        var artefacts = repository.selectAllArtefacts();
        ArrayList<List<CloneArtefact>> list = new ArrayList<>();
        artefacts.forEach(art -> {
            var scores = repository.selectBestScores(art.id());
            list.add(scores.stream().map(score -> {
                var art2 = repository.selectArtById(score.id1() == art.id() ? score.id2() : score.id1());
                return new CloneArtefact(art2, score.score());
            }).toList());
        });
        response.send(list);
    }

    /**
     *
     * @param request the server request
     * @param response the server response
     */
    private void getBestScoresById(ServerRequest request, ServerResponse response) {
        var id = Integer.parseInt(request.path().param("id"));
        var scores = repository.selectBestScores(id);
        var artefacts = scores.stream().map(score -> repository.selectArtById(score.id2())).toList();
        var it = artefacts.iterator();
        response.send(scores.stream().map(score -> new CloneArtefact(it.next(), score.score())).toList());
    }


    /**
     *
     * @param request the server request
     * @param response the server response
     */
    private void getCloneSource(ServerRequest request, ServerResponse response){
        var id = Integer.parseInt(request.path().param("id"));
        var res = repository.selectClonesOfArtifact(id);
        var artClones = new HashMap<Artefact, List<List<List<String>>>>();

        var map = res.stream().collect(Collectors.groupingBy(Clone::idClone, Collectors.toList()));
        map.forEach((key, value) -> {
            var jar1 = repository.selectJarByIdHash(value.get(0).id1());
            var jar2 = repository.selectJarByIdHash(value.get(0).id2());
            var art2 = repository.selectArtById(id == jar1.idJar() ? jar2.idJar() : jar1.idJar());
            artClones.computeIfAbsent(art2, k -> new ArrayList<>()).add(forEachClone(id, jar1.idJar(), value));
        });

        response.send(artClones.entrySet().stream().map(entry -> new CloneSources(entry.getKey(), entry.getValue())).toList());
    }

    private List<List<String>> forEachClone(long id, long idJar, List<Clone> clones) {
        var clone1 = clones.get(0);
        var clone2 = clones.size() == 1 ? clone1 : clones.get(1);
        return id == idJar ? getSourceOfClone(clone1.id1(), clone1.id2(), clone2.id1(), clone2.id2()) :
                getSourceOfClone(clone1.id2(), clone1.id1(), clone2.id2(), clone2.id1());
    }


    private List<List<String>> getSourceOfClone(long clone1id1, long clone1id2, long clone2id1, long clone2id2){
        var jar1 = repository.selectJarByIdHash(clone1id1);
        var jar2 = repository.selectJarByIdHash(clone1id2);

        var id1instr1 = repository.selectInstrById(clone1id1);
        var id2instr1 = repository.selectInstrById(clone1id2);
        var id1instr2 = repository.selectInstrById(clone2id1);
        var id2instr2 = repository.selectInstrById(clone2id2);

        var lines1 = repository.getLinesOfClone(id1instr1.idHash(), id1instr2.idHash());
        var lines2 = repository.getLinesOfClone(id2instr1.idHash(), id2instr2.idHash());

        return (List.of(SourcesReader.extractLines(jar1.sources(), id1instr1.file(), lines1.get("min"), lines1.get("max")),
                SourcesReader.extractLines(jar2.sources(), id2instr1.file(), lines2.get("min"), lines2.get("max"))));
    }


}
