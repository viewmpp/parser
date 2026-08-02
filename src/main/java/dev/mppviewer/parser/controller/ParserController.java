package dev.mppviewer.parser.controller;

import dev.mppviewer.parser.model.dto.ProjectDTO;
import dev.mppviewer.parser.service.ParseGate;
import dev.mppviewer.parser.service.ProjectParser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;


@RestController
public class ParserController {

    private final ProjectParser projectParser;
    private final ParseGate gate;

    public ParserController(ProjectParser projectParser, ParseGate gate) {
        this.projectParser = projectParser;
        this.gate = gate;
    }

    @PostMapping(value = "/parse", consumes = APPLICATION_OCTET_STREAM_VALUE)
    public ProjectDTO parse(@RequestBody byte[] body) {
        return gate.run(() -> projectParser.parse(body));
    }
}
