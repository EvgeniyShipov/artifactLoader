package ru.sbt.integration.orchestration.exception;

/**
 * Ошибка при работе с мавен репозиторием
 */
public class RepoWorkerException extends Exception {
    public RepoWorkerException(String message) {
        super(message);
    }

    public RepoWorkerException(String message, Exception e) {
        super(message, e);
    }
}
