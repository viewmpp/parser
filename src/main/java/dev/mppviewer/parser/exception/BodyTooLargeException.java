package dev.mppviewer.parser.exception;


public class BodyTooLargeException extends RuntimeException {

    public BodyTooLargeException(long declaredBytes, long maxBytes) {
        super("declared body size " + declaredBytes + " bytes exceeds limit of " + maxBytes);
    }
}
