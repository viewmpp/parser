package dev.mppviewer.parser.model.dto;

import java.util.List;


public record ProjectDTO(
        int contractVersion,
        ProjectInfoDTO project,
        CalendarDTO calendar,
        List<ResourceDTO> resources,
        List<TaskDTO> tasks,
        List<RelationDTO> relations
) {
}
