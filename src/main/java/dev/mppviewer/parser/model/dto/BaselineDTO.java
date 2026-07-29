package dev.mppviewer.parser.model.dto;

import java.time.LocalDateTime;


public record BaselineDTO(
        LocalDateTime start,
        LocalDateTime finish
) {
}
