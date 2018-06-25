package ru.sbt.integration.orchestration.dependency;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import ru.sbt.integration.orchestration.exception.DependencyLoaderException;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DependencyLoaderImpl implements DependencyLoader {
    private final CollectRequest collectRequest = new CollectRequest();
    private final DependencyRequest request = new DependencyRequest();
    private final DefaultServiceLocator locator = getServiceLocator();
    private final DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
    private final PreorderNodeListGenerator preorderNodeListGenerator = new PreorderNodeListGenerator();

    @Override
    public String resolveDependencies(Set<MavenArtifact> artifacts, Set<String> repositories, String localRepoPath) throws DependencyLoaderException {
        collectRepositories(repositories);
        uploadArtifactWithDependencies(artifacts, localRepoPath);
        return preorderNodeListGenerator.getClassPath();
    }

    @Override
    public String resolveDependencies(MavenArtifact artifact, Set<String> repositories, String localRepoPath) throws DependencyLoaderException {
        Set<MavenArtifact> artifacts = new HashSet<>();
        artifacts.add(artifact);
        return resolveDependencies(artifacts, repositories, localRepoPath);
    }

    private void collectRepositories(Set<String> repoList) {
        collectRequest.setRepositories(repoList.stream()
                .map(url -> new RemoteRepository.Builder(getRepositoryId(url), "default", url).build())
                .distinct()
                .collect(Collectors.toList()));
    }

    private RepositorySystem getRepositorySystem(String localRepositoryPath, DefaultServiceLocator locator, DefaultRepositorySystemSession session) {
        RepositorySystem system = locator.getService(RepositorySystem.class);
        LocalRepository localRepository = new LocalRepository(localRepositoryPath);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepository));
        return system;
    }

    private DefaultServiceLocator getServiceLocator() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        return locator;
    }

    private void uploadArtifactWithDependencies(Set<MavenArtifact> artifacts, String localRepositoryPath) throws DependencyLoaderException {
        for (MavenArtifact artifact : artifacts) {
            if (!existInLocalRepo(artifact, localRepositoryPath)) {
                getSourcesArtifact(artifact, localRepositoryPath);
                getJarArtifact(artifact, localRepositoryPath);
            }
        }
    }

    private boolean existInLocalRepo(MavenArtifact artifact, String localRepositoryPath) {
        String path = localRepositoryPath + "/" +
                (artifact.getGroupId() + "." + artifact.getArtifactId()).replace(".", "/") + "/" + artifact.getVersion();
        File dir = new File(path);
        if (dir.exists() && dir.listFiles() != null)
            return Arrays.stream(dir.listFiles()).map(File::getName).anyMatch(name -> name.endsWith(".jar"))
                    && Arrays.stream(dir.listFiles()).map(File::getName).anyMatch(name -> name.endsWith("-sources.jar"));
        return false;
    }

    private void getSourcesArtifact(MavenArtifact artifact, String localRepositoryPath) throws DependencyLoaderException {
        try {
            DefaultArtifact art = new DefaultArtifact(String.format("%s:%s:%s:%s:%s",
                    artifact.getGroupId(), artifact.getArtifactId(), "jar", "sources", artifact.getVersion()));
            uploadArtifactWithDependencies(art, localRepositoryPath);
        } catch (DependencyCollectionException | DependencyResolutionException e) {
            throw new DependencyLoaderException(
                    String.format("%s-sources.jar, или одна из его зависимостей, отсутствует в репозиториях: %s",
                            artifact.getArtifactId() + "-" + artifact.getVersion(), collectRequest.getRepositories()));
        }
    }

    //todo вынести в JarArtifact и SourceArtifact. Отнаследовать MavenArtifact от DefaultArtifact
    private void getJarArtifact(MavenArtifact artifact, String localRepositoryPath) throws DependencyLoaderException {
        try {
            DefaultArtifact art = new DefaultArtifact(String.format("%s:%s:%s",
                    artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
            uploadArtifactWithDependencies(art, localRepositoryPath);
        } catch (DependencyCollectionException | DependencyResolutionException e) {
            throw new DependencyLoaderException(
                    String.format("%s.jar, или одна из его зависимостей, отсутствует в репозиториях: %s",
                            artifact.getArtifactId() + "-" + artifact.getVersion(), collectRequest.getRepositories()));
        }
    }

    private void uploadArtifactWithDependencies(DefaultArtifact artifact, String localRepositoryPath)
            throws DependencyLoaderException, DependencyResolutionException, DependencyCollectionException {
        Dependency dependency = new Dependency(artifact, "compile");
        collectRequest.setRoot(dependency);
        RepositorySystem system = getRepositorySystem(localRepositoryPath, locator, session);
        DependencyNode dependencyNode = system.collectDependencies(session, collectRequest).getRoot();
        request.setRoot(dependencyNode);
        system.resolveDependencies(session, request);
        dependencyNode.accept(preorderNodeListGenerator);
    }

    private String getRepositoryId(String url) {
        if (url.endsWith("/")) {
            String part = url.substring(0, url.length() - 1);
            return part.substring(part.lastIndexOf("/") + 1);
        } else {
            return url.substring(url.lastIndexOf("/") + 1);
        }
    }
}
