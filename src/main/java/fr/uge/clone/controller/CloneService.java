package fr.uge.clone.controller;

import fr.uge.clone.repository.CloneRepository;
import fr.uge.clone.analyze.SourcesJar;
import fr.uge.clone.analyze.Analyzer;
import fr.uge.clone.analyze.CloneDetector;
import fr.uge.clone.model.Clone;
import fr.uge.clone.model.CloneArtefact;
import fr.uge.clone.model.Jar;
import io.helidon.common.http.Http;
import io.helidon.dbclient.DbClient;
import io.helidon.webserver.*;

import java.beans.BeanProperty;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
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

    @Override
    public void update(Routing.Rules rules)  {
        rules.get("/artefacts", this::getArtefacts)
                .get("/artefact/{id}", this::getArtefactById)
                .get("/clones/{id}", this::getCloneSource)
                .get("/scores/{id}", this::getBestScoresById)
                .get("/allscores", this::getAllBestScores)
                .post("/post/{type}", this::insertArtefact)
        .post("/class/UploadComplete", this::completeInsertion);
    }


    /**
     *
     * @param serverRequest
     * @param serverResponse
     */
    public void getArtefacts(ServerRequest serverRequest, ServerResponse serverResponse) {
        serverResponse.send(repository.selectAllArtefacts());
    }

    /**
     *
     * @param request
     * @param response
     */
    public void completeInsertion(ServerRequest request, ServerResponse response){
        repository.insertJar(new ByteArrayInputStream(map.get("classes")), new ByteArrayInputStream(map.get("sources")));

        var lastJar = repository.selectLastJar();
        var dataMap = SourcesJar.getAllData(lastJar.sources());
        map.clear();

        repository.insertArtefact(lastJar.idJar(), dataMap.get("name"), dataMap.get("url"));
        repository.insertMetadata(lastJar.idJar(), dataMap.get("groupId"), dataMap.get("artifactId"), dataMap.get("version"));

        response.send(Http.Status.OK_200).thenAcceptAsync(r -> {
            new Analyzer(dbClient, lastJar.classes(), lastJar.idJar()).launch();
            System.out.println("idJar2 : " + lastJar.idJar());
            new CloneDetector(dbClient, lastJar.idJar()).detect();
        });

    }


    /**
     *
     * @param request
     * @param response
     */
    public void insertArtefact(ServerRequest request, ServerResponse response) {
        var type = request.path().param("type");

        request.content().map(byteBuffers -> byteBuffers.bytes()).collectList()
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
     * @param request
     * @param response
     */
    private void getArtefactById(ServerRequest request, ServerResponse response) {
        var id = Integer.parseInt(request.path().param("id"));
        var metadata = repository.selectMetaDataById(id);
        System.out.println(metadata.get());
        response.send(metadata.isEmpty() ? "error" : List.of(metadata.get()));
    }


    /**
     *
     * @param idHash
     * @return
     */
    private Jar getJarByIdHash(long idHash){
        var id = repository.selectInstrById(idHash).id();
        return repository.selectJarById(id);
    }

    /**
     *
     * @param request
     * @param response
     */
    private void getAllBestScores(ServerRequest request, ServerResponse response) {
        var artefacts = repository.selectAllArtefacts();
        ArrayList<List<CloneArtefact>> list = new ArrayList<>();
        artefacts.stream().forEach(art -> {
            var scores = repository.selectBestScores(art.id());
            list.add(scores.stream().map(score -> new CloneArtefact(art, score.score())).toList());
        });
        response.send(list);
    }

    /**
     *
     * @param request
     * @param response
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
     * @param request
     * @param response
     */
    private void getBestScoresArtefacts(ServerRequest request, ServerResponse response) {
        var id = Integer.parseInt(request.path().param("id"));
        var res = repository.selectBestScores(id).stream().map(score -> repository.selectArtById(score.id2())).toList();
        response.send(res);
    }

    /**
     *
     * @param request
     * @param response
     */
    private void getCloneSource(ServerRequest request, ServerResponse response){
        var id = Integer.parseInt(request.path().param("id"));
        var list = new ArrayList<List<List<List<String>>>>();
        try {
            var res = dbClient.execute(dbExecute -> dbExecute.createNamedQuery("get-clone-of-art")
                    .addParam("id1", id).addParam("id2", id).execute()
                    .map(dbRow -> dbRow.as(Clone.class))).collectList().get();

            var map = res.stream().collect(Collectors.groupingBy(clone -> clone.idClone(), Collectors.toList()));
            map.entrySet().forEach(clone -> {
                var jar1 = getJarByIdHash(clone.getValue().get(0).id1());
                var jar2 = getJarByIdHash(clone.getValue().get(0).id2());

                /************************************************************************/
                var id1instr1 = repository.selectInstrById(clone.getValue().get(0).id1());
                var id2instr1 = repository.selectInstrById(clone.getValue().get(0).id2());
                var id1instr2 = clone.getValue().size() == 1 ? id1instr1 :
                        repository.selectInstrById(clone.getValue().get(1).id1());
                var id2instr2 = clone.getValue().size() == 1 ? id2instr1 :
                        repository.selectInstrById(clone.getValue().get(1).id2());

                list.add(List.of(SourcesJar.extractLines(jar1.sources(), id1instr1.file(), id1instr1.line(), id1instr2.line()),
                                SourcesJar.extractLines(jar2.sources(), id2instr1.file(), id2instr1.line(), id2instr2.line())));

            });

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        response.send(list);
    }


}
