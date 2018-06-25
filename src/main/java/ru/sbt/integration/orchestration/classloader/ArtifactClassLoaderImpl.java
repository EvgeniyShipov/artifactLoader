package ru.sbt.integration.orchestration.classloader;

import ru.sbt.integration.orchestration.FileHelper;
import ru.sbt.integration.orchestration.dependency.DependencyLoader;
import ru.sbt.integration.orchestration.dependency.DependencyLoaderImpl;
import ru.sbt.integration.orchestration.dependency.MavenArtifact;
import ru.sbt.integration.orchestration.exception.DependencyLoaderException;
import ru.sbt.integration.orchestration.exception.FailedReadResourceException;
import ru.sbt.integration.orchestration.resourcereader.ResourceReader;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by SBT-Vitchinkin-AV on 14.09.2017.
 */
public class ArtifactClassLoaderImpl implements ArtifactClassLoader {

    private final DependencyLoader dependencyLoader = new DependencyLoaderImpl();
    private static final Set<String> defaultRepositories = new HashSet<>(50, 1.0F);
    private static final String REPO_RESOURCE_PATH = "repositories.txt";
    private static final String repoPath = "../temp/localRepo";

    private static ArtifactClassLoader instance;
    private static volatile Boolean isInitiated = false;

    private ArtifactClassLoaderImpl() throws FailedReadResourceException {
    }

    public static ArtifactClassLoader getInstance() throws FailedReadResourceException {
        if (!isInitiated) {
            synchronized (ArtifactClassLoaderImpl.class) {
                if (!isInitiated) {
                    try {
                       /* Properties properties = new Properties();
                        properties.load(new FileInputStream("src/main/resources/context.properties"));
                         repoPath = properties.getProperty("DEFAULT_LOCAL_REPO_PATH");*/

                        instance = new ArtifactClassLoaderImpl();
                        defaultRepositories.addAll(new ResourceReader().readResource(REPO_RESOURCE_PATH));
                        isInitiated = true;

                    } catch (IOException | URISyntaxException | NullPointerException e) {
                        e.printStackTrace();
                        throw new FailedReadResourceException(String.format("Не удалось загрузить список репозиториев(%s)", REPO_RESOURCE_PATH), e);
                    }
                }
            }
        }
        return instance;
    }

    public static String getRepoPath() {
        return repoPath;
    }

    @Override
    public List<Class<?>> getClasses(Set<MavenArtifact> artifacts, Set<String> repositories) throws IOException, DependencyLoaderException {
        return getClasses(artifacts, repositories, repoPath);
    }

    @Override
    public Path getJarPath(MavenArtifact artifact, Set<String> repositories) throws DependencyLoaderException, IOException {
        if (repositories != null && repositories.size() > 0)
            defaultRepositories.addAll(repositories);
        dependencyLoader.resolveDependencies(artifact, defaultRepositories, repoPath);
        return FileHelper.getPath(repoPath, file ->
                file.toString().endsWith((artifact.getArtifactId() + "-" + artifact.getVersion() + ".jar")));
    }

    @Override
    public Path getSourceJarPath(MavenArtifact artifact, Set<String> repositories) throws DependencyLoaderException, IOException {
        if (repositories != null && repositories.size() > 0)
            defaultRepositories.addAll(repositories);
        dependencyLoader.resolveDependencies(artifact, defaultRepositories, repoPath);
        return FileHelper.getPath(repoPath, file ->
                file.toString().endsWith((artifact.getArtifactId() + "-" + artifact.getVersion() + "-sources.jar")));
    }

    private List<Class<?>> getClasses(Set<MavenArtifact> artifacts, Set<String> repositories, String localRepoPath) throws DependencyLoaderException, IOException {
        repositories.addAll(defaultRepositories);
        dependencyLoader.resolveDependencies(artifacts, repositories, localRepoPath);
        List<String> jarPaths = FileHelper.getFilePaths(localRepoPath, file -> file.toString().endsWith(".jar"));
        ClassLoader cl = getClassLoaderForJarFile(jarPaths.toArray(new String[jarPaths.size()]));
        List<Class<?>> classList = new ArrayList<>();

        for (MavenArtifact artifact : artifacts) {
            getJarFile(jarPaths, cl, classList, artifact);
        }

        return classList;
    }

    private ClassLoader getClassLoaderForJarFile(String... paths) throws MalformedURLException {
        URL[] classLoaderUrls = new URL[paths.length];
        for (int i = 0; i < paths.length; i++) {
            File file = new File(paths[i]);
            classLoaderUrls[i] = file.toURI().toURL();
        }
        return new URLClassLoader(classLoaderUrls);
    }

    private void getJarFile(List<String> jarPaths, ClassLoader loader, List<Class<?>> classList, MavenArtifact artifact) {
        jarPaths.stream()
                .filter(s -> s.contains(artifact.getArtifactId() + "-" + artifact.getVersion() + ".jar"))
                .findFirst()
                .ifPresent(jarFileName -> loadClasses(jarFileName, loader, classList));
    }

    private void loadClasses(String fileName, ClassLoader loader, List<Class<?>> classList) {
        try (JarFile jar = new JarFile(fileName)) {
            jar.stream()
                    .filter(Objects::nonNull)
                    .map(JarEntry::toString)
                    .filter(entry -> entry.endsWith(".class"))
                    .forEach(clazz -> loadClass(loader, classList, clazz));
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private void loadClass(ClassLoader loader, List<Class<?>> classList, String clazz) {
        //todo заменить printStacktrace и обернуть в ошибку ArtifactClassLoader
        try {
            Class<?> loadedClass = loader.loadClass(clazz.replace("/", ".").replace(".class", ""));
            classList.add(loadedClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
