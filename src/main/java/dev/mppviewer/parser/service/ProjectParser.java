package dev.mppviewer.parser.service;

import dev.mppviewer.parser.model.dto.ProjectDTO;


public interface ProjectParser {
    ProjectDTO parse(byte[] source);
}
