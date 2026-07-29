package dev.mppviewer.parser.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ParserController {

    @GetMapping("/parse")
    public ResponseEntity<?> parse() {
        return new ResponseEntity<>("data", HttpStatus.OK);
    }
}
