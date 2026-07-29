package dev.mppviewer.parser;

import dev.mppviewer.parser.model.dto.ProjectDTO;
import dev.mppviewer.parser.service.ProjectParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class LargeProjectTest {

    @Autowired
    private ProjectParser parser;

    @Test
    void parsesLargeProject() throws IOException {
        byte[] bytes = Files.readAllBytes(Path.of("testdata", "large_1500.xml"));

        parser.parse(bytes);

        long start = System.nanoTime();
        ProjectDTO dto = parser.parse(bytes);
        long millis = (System.nanoTime() - start) / 1_000_000;

        System.out.println("large_1500: " + dto.tasks().size() + " tasks, "
                + dto.relations().size() + " relations, " + millis + " ms");

        assertThat(dto.tasks()).hasSizeGreaterThan(1000);
    }
}
