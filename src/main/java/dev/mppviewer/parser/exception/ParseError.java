package dev.mppviewer.parser.exception;


public enum ParseError {

    UNSUPPORTED_FORMAT("unsupported_format", "file is not a recognised project file"),
    CORRUPT_FILE("corrupt_file", "project file is damaged and could not be read"),
    BODY_TOO_LARGE("body_too_large", "file is too large"),
    LENGTH_REQUIRED("length_required", "request must declare its size"),
    PARSER_BUSY("parser_busy", "parser is busy, try again shortly"),
    BAD_REQUEST("bad_request", "request could not be processed"),
    INTERNAL("internal_error", "internal error");

    private final String code;
    private final String message;

    ParseError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
