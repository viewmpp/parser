package dev.mppviewer.parser.controller;

import dev.mppviewer.parser.service.ProjectParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;


@RestController
public class ParserController {

    private final ProjectParser projectParser;

    public ParserController(ProjectParser projectParser) {
        this.projectParser = projectParser;
    }

    @PostMapping(value = "/parse", consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> parse(@RequestBody byte[] body) {
        return new ResponseEntity<>(projectParser.parse(body), HttpStatus.OK);
    }
}
