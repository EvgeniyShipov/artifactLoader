package ru.sbt.integration.orchestration.classloader;

import ru.sbt.integration.orchestration.dependency.MavenArtifact;
import ru.sbt.integration.orchestration.exception.DependencyLoaderException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Получание списка классов из мавен артефактов
 */
public interface ArtifactClassLoader {

    /**
     * Метод возвращает список классов из мавен артефакта.
     * В случае, если repositories пустой, или null, используется дефольный список репозиториев.
     *
     * @param artifacts    мавен артефакты
     * @param repositories список репозиториев
     * @return
     * @throws IOException
     */
    List<Class<?>> getClasses(Set<MavenArtifact> artifacts, Set<String> repositories) throws DependencyLoaderException, IOException;

    /**
     * Метод возвращает Path до source.jar загруженного артефакта, в локальном репозитории
     *
     * @param artifact     мавен артефакт
     * @param repositories список репозиториев
     * @return
     * @throws IOException
     */
    Path getSourceJarPath(MavenArtifact artifact, Set<String> repositories) throws DependencyLoaderException, IOException;

    /**
     * Метод возвращает Path до .jar загруженного артефакта, в локальном репозитории
     *
     * @param artifact     мавен артефакт
     * @param repositories список репозиториев
     * @return
     * @throws IOException
     */
    Path getJarPath(MavenArtifact artifact, Set<String> repositories) throws DependencyLoaderException, IOException;
}
