package ru.sbt.integration.orchestration.classloader;

import org.junit.Assert;
import org.junit.Test;
import ru.sbt.integration.orchestration.dependency.MavenArtifact;
import ru.sbt.integration.orchestration.exception.DependencyLoaderException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by SBT-Vitchinkin-AV on 14.09.2017.
 */
public class ArtifactClassLoaderImplTest {

    @Test
    public void getClassesForArtifact() throws Exception {
        MavenArtifact artifact = new MavenArtifact("com.sbt.pprb.dto", "creditcardstatement-dto", "1.0.2");

        Set<String> repositories = new HashSet<>();
        repositories.add("http://sbtnexus.ca.sbrf.ru:8081/nexus/content/repositories/INTLAB_release");

        List<Class<?>> cl = ArtifactClassLoaderImpl.getInstance().getClasses(Collections.singleton(artifact), repositories);

        Assert.assertNotNull(cl);
        Assert.assertFalse(cl.size() == 1);
    }

    @Test
    public void loadFileTest() throws IOException, DependencyLoaderException {
        ArtifactClassLoader classLoader = ArtifactClassLoaderImpl.getInstance();
        MavenArtifact mavenArtifact = new MavenArtifact("com.sbt.pprb.dto", "creditcardstatement-dto", "1.0.3");
        Path path = classLoader.getJarPath(mavenArtifact, null);
        Assert.assertTrue(path.getFileName().toString().endsWith("creditcardstatement-dto-1.0.3.jar"));
        Path sourcePath = classLoader.getSourceJarPath(mavenArtifact, null);
        Assert.assertTrue(sourcePath.getFileName().toString().endsWith("creditcardstatement-dto-1.0.3-sources.jar"));
    }
}