public class CloneMapper implements DbMapper<Artefact> {

    @Override
    public Artefact read(DbRow row) {
        DbColumn groupId = row.column("groupId");
        return new Artefact(groupId.as(String.class));
    }

}
