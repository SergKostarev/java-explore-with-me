package ru.practicum.exception;

public class NotFoundException extends RuntimeException {
    private final Long id;

    public NotFoundException(String message, Long id) {
        super(message);
        this.id = id;
    }

}
