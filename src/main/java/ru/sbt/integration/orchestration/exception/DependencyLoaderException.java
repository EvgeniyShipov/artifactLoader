package ru.sbt.integration.orchestration.exception;

public class DependencyLoaderException extends Exception {
    public DependencyLoaderException() {
        super();
    }

    public DependencyLoaderException(String s) {
        super(s);
    }

    public DependencyLoaderException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
