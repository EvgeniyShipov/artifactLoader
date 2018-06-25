package ru.sbt.integration.orchestration.exception;

public class FailedReadResourceException extends RuntimeException {
    public FailedReadResourceException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public FailedReadResourceException(String s) {
        super(s);
    }
}
