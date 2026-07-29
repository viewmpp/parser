package dev.mppviewer.parser.model.dto;

import java.time.LocalDateTime;
import java.util.List;


public record TaskDTO(
        Integer id,
        Integer parentId,
        String name,
        String wbs,
        String outlineNumber,
        Integer outlineLevel,
        LocalDateTime start,
        LocalDateTime finish,
        DurationDTO duration,
        double percentComplete,
        boolean isSummary,
        boolean isMilestone,
        boolean isCritical,
        String notes,
        BaselineDTO baseline,
        List<AssignmentDTO> assignments
) {
}
