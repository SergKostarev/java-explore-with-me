package exception;

import lombok.Getter;

@Getter
public class IncorrectDateException extends RuntimeException {

    private final String data;

    public IncorrectDateException(final String message, String data) {
        super(message);
        this.data = data;
    }
}
