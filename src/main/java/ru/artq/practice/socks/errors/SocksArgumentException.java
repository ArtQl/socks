package ru.artq.practice.socks.errors;

public class SocksArgumentException extends RuntimeException {
    public SocksArgumentException(String message) {
        super(message);
    }
}
