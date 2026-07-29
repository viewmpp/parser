package dev.mppviewer.parser.model.dto;

import java.time.LocalDateTime;


public record ProjectInfoDTO(
        String name,
        LocalDateTime start,
        LocalDateTime finish
) {
}
