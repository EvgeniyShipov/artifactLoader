package ru.sbt.integration.orchestration.dependency;

import org.junit.Assert;
import org.junit.Test;
import ru.sbt.integration.orchestration.exception.DependencyLoaderException;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class DependencyLoaderImplTest {

    @Test
    public void resolveDependencies() throws DependencyLoaderException {
        MavenArtifact artifact = new MavenArtifact("com.sbt.pprb.dto", "creditcardstatement-dto", "1.0.3");
        String repoPath = "target/downloaded-sources";
        Set<String> repositories = new HashSet<>();
        repositories.add("http://sbtnexus.ca.sbrf.ru:8081/nexus/content/repositories/INTLAB_release");
        DependencyLoaderImpl dependencyLoader = new DependencyLoaderImpl();
        dependencyLoader.resolveDependencies(artifact, repositories, repoPath);
        Assert.assertTrue(Paths.get(repoPath).toFile().exists());
    }
}