package dev.mppviewer.parser.model.dto;

import java.util.List;


public record CalendarDTO(
        String name,
        List<String> nonWorkingWeekdays,
        List<CalendarExceptionDTO> exceptions
) {
}
