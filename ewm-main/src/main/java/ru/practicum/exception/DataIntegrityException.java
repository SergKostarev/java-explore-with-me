package ru.practicum.exception;

import lombok.Getter;

@Getter
public class DataIntegrityException extends RuntimeException {

    private final String data;

    public DataIntegrityException(final String message, String data) {
        super(message);
        this.data = data;
    }
}