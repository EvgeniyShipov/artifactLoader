package ru.sbt.integration.orchestration.dependency;

import ru.sbt.integration.orchestration.exception.DependencyLoaderException;

import java.util.Set;

/**
 * Осуществляет загрузку мавен артефактов
 */
public interface DependencyLoader {

    /**
     * @param artifacts     список мавен артефактов
     * @param repositories      список мавен репозиториев
     * @param localRepoPath локальное хранилище для подгруженных артефактов
     * @return
     * @throws DependencyLoaderException
     */
    String resolveDependencies(Set<MavenArtifact> artifacts, Set<String> repositories, String localRepoPath) throws DependencyLoaderException;

    String resolveDependencies(MavenArtifact artifact, Set<String> repositories, String localRepoPath) throws DependencyLoaderException;
}
