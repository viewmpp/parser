package dev.mppviewer.parser.model.dto;

import java.time.LocalDateTime;


public record CalendarExceptionDTO(
        LocalDateTime from,
        LocalDateTime to,
        boolean working,
        String name
) {
}
