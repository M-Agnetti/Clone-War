package fr.uge.clone.model;

/**
 * Record representing all the Metadata from
 * a project from its pom.xml file.
 * @param idMeta the id of the Metadata
 * @param groupId the groupId of the project
 * @param artifactId the artifactId of the project
 * @param version the version of the project
 */
public record MetaData(long idMeta, String groupId, String artifactId, String version) {
}
