package ru.practicum.exception;

import lombok.Getter;

@Getter
public class ConditionsNotMetException extends RuntimeException {

    private final String data;

    public ConditionsNotMetException(final String message, String data) {
        super(message);
        this.data = data;
    }
}
