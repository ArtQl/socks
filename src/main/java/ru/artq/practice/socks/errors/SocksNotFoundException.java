package ru.artq.practice.socks.errors;

public class SocksNotFoundException extends RuntimeException {

    public SocksNotFoundException(String message) {
        super(message);
    }
}
