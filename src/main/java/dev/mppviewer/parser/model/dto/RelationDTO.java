package dev.mppviewer.parser.model.dto;


public record RelationDTO(
        int id,
        Integer predecessorId,
        Integer successorId,
        String type,
        DurationDTO lag
) {
}
