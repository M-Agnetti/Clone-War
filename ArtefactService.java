

public class ArtefactService implements Service {

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/subpath", this::getHandler);
    }

    private void getHandler(ServerRequest request,
                            ServerResponse response) {
        // Some logic
    }
}
