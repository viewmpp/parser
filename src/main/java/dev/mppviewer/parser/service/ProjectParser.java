package dev.mppviewer.parser.util;

import dev.mppviewer.parser.model.dto.ProjectDTO;
import org.springframework.stereotype.Component;


@Component
public interface ProjectParser {
    ProjectDTO parse(byte[] source);
}
