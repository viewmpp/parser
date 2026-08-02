package dev.mppviewer.parser.exception;


public class ParserBusyException extends RuntimeException {

    public ParserBusyException(int slots) {
        super("all " + slots + " parse slots are busy");
    }
}
