package dev.mppviewer.parser.exception;


public class LengthRequiredException extends RuntimeException {

    public LengthRequiredException() {
        super("request has no Content-Length");
    }
}
