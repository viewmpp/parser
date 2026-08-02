package dev.mppviewer.parser.exception;

import dev.mppviewer.parser.model.dto.ExceptionDTO;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@Slf4j
@ControllerAdvice
public class ParserExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({InvalidProjectFileException.class})
    public ResponseEntity<ExceptionDTO> handleInvalidProjectFile(InvalidProjectFileException ex) {
        log.info("file rejected [{}]: {}", ex.error().code(), ex.getMessage());
        return build(ex.error(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({LibraryFailureException.class})
    public ResponseEntity<ExceptionDTO> handleLibraryFailure(LibraryFailureException ex) {
        log.error("library failed while reading a file", ex);
        return build(ParseError.CORRUPT_FILE, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({BodyTooLargeException.class})
    public ResponseEntity<ExceptionDTO> handleBodyTooLarge(BodyTooLargeException ex) {
        log.info("body rejected: {}", ex.getMessage());
        return build(ParseError.BODY_TOO_LARGE, HttpStatus.CONTENT_TOO_LARGE);
    }

    @ExceptionHandler({LengthRequiredException.class})
    public ResponseEntity<ExceptionDTO> handleLengthRequired(LengthRequiredException ex) {
        log.info("request rejected: {}", ex.getMessage());
        return build(ParseError.LENGTH_REQUIRED, HttpStatus.LENGTH_REQUIRED);
    }

    @ExceptionHandler({ParserBusyException.class})
    public ResponseEntity<ExceptionDTO> handleParserBusy(ParserBusyException ex) {
        log.warn("request rejected: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.RETRY_AFTER, "30")
                .body(new ExceptionDTO(ParseError.PARSER_BUSY.code(), ParseError.PARSER_BUSY.message()));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ExceptionDTO> handleException(Exception ex) {
        log.error("unhandled exception", ex);
        return build(ParseError.INTERNAL, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             @NonNull HttpHeaders headers,
                                                             HttpStatusCode status,
                                                             @NonNull WebRequest request) {
        log.warn("request rejected: status={}, detail={}", status.value(), ex.getMessage());
        ParseError error = status.is5xxServerError() ? ParseError.INTERNAL : ParseError.BAD_REQUEST;
        return new ResponseEntity<>(new ExceptionDTO(error.code(), error.message()), headers, status);
    }

    private ResponseEntity<ExceptionDTO> build(ParseError error, HttpStatus status) {
        return new ResponseEntity<>(new ExceptionDTO(error.code(), error.message()), status);
    }
}
