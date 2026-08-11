package dev.mppviewer.parser.controller;

import dev.mppviewer.parser.model.dto.HealthDTO;
import dev.mppviewer.parser.util.Vcs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HealthController {

    @Value("${parser.env}")
    private String env;

    @GetMapping("/healthcheck")
    public HealthDTO healthcheck() {
        return new HealthDTO("OK", env, Vcs.version());
    }
}
