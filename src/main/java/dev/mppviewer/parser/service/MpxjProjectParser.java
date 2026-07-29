package dev.mppviewer.parser.service;

import dev.mppviewer.parser.model.dto.ProjectDTO;
import org.springframework.stereotype.Service;


@Service
public class MpxjProjectParser implements ProjectParser {

    @Override
    public ProjectDTO parse(byte[] source) {
        return null;
    }
}
