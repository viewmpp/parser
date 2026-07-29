package dev.mppviewer.parser.controller;

import dev.mppviewer.parser.model.dto.HealthDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HealthController {

    @GetMapping("/health")
    public HealthDTO health() {
        return new HealthDTO("UP");
    }
}
