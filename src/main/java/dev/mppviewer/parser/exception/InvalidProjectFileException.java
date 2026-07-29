package dev.mppviewer.parser.exception;


public class InvalidProjectFileException extends RuntimeException {

    private final ParseError error;

    public InvalidProjectFileException(ParseError error, String detail, Throwable cause) {
        super(detail, cause);
        this.error = error;
    }

    public ParseError error() {
        return error;
    }
}
