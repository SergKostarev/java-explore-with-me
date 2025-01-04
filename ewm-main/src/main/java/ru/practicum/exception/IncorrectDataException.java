package ru.practicum.exception;

import lombok.Getter;

@Getter
public class IncorrectDataException extends RuntimeException {

    private final String data;

    public IncorrectDataException(final String message, String data) {
        super(message);
        this.data = data;
    }
}