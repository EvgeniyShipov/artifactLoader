package ru.sbt.integration.orchestration.dependency;

import java.util.Objects;

public class MavenArtifact {
    private final String groupId;
    private final String artifactId;
    private final String version;

    public MavenArtifact(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MavenArtifact))
            return false;
        MavenArtifact that = (MavenArtifact) o;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    public String toGAVkey() {
        return groupId + ":" + artifactId + ":" + version;
    }

    public static MavenArtifact fromGAVkey(String art) {
        String[] arts = art.split("[:]");
        return new MavenArtifact(arts[0], arts[1], arts[2]);
    }
}
