package ru.artq.practice.socks.errors;

public class CsvProcessingException extends RuntimeException {

    public CsvProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
