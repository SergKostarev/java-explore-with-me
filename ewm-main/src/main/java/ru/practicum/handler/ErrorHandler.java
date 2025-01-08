package ru.practicum.handler;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.dto.ApiError;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.DataIntegrityException;
import ru.practicum.exception.IncorrectDataException;
import ru.practicum.exception.NotFoundException;
import utils.DateUtils;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        return new ApiError(e.getMessage(), "The required object was not found.",
                HttpStatus.NOT_FOUND.toString(), DateUtils.convertToString(LocalDateTime.now()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException e) {
        return new ApiError(e.getMessage(), "Incorrectly made request.",
                HttpStatus.BAD_REQUEST.toString(), DateUtils.convertToString(LocalDateTime.now()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrity(DataIntegrityException e) {
        return new ApiError(e.getMessage(), "Integrity constraint has been violated.",
                HttpStatus.CONFLICT.toString(), DateUtils.convertToString(LocalDateTime.now()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIncorrectData(IncorrectDataException e) { // TODO merge with handleConstraintViolation
        return new ApiError(e.getMessage(), "Incorrectly made request.",
                HttpStatus.BAD_REQUEST.toString(), DateUtils.convertToString(LocalDateTime.now()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConditionsNotMet(ConditionsNotMetException e) {
        return new ApiError(e.getMessage(), "For the requested operation the conditions are not met.",
                HttpStatus.CONFLICT.toString(), DateUtils.convertToString(LocalDateTime.now()));
    }

}