package dev.mppviewer.parser;

import dev.mppviewer.parser.model.dto.ProjectDTO;
import dev.mppviewer.parser.model.dto.RelationDTO;
import dev.mppviewer.parser.model.dto.TaskDTO;
import dev.mppviewer.parser.service.ProjectParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class ContractComplianceTest {

    private static final Path CORPUS = Path.of("testdata", "real");

    @Autowired
    private ProjectParser parser;

    static Stream<Path> corpus() throws IOException {
        try (var files = Files.list(CORPUS)) {
            return files.filter(p -> p.toString().endsWith(".mpp")).sorted().toList().stream();
        }
    }

    @ParameterizedTest
    @MethodSource("corpus")
    void everyCorpusFileParses(Path file) throws IOException {
        ProjectDTO dto = parser.parse(Files.readAllBytes(file));

        assertThat(dto.tasks()).isNotEmpty();
        assertThat(dto.contractVersion()).isEqualTo(1);

        for (TaskDTO task : dto.tasks()) {
            assertThat(task.id()).isNotNull();
            assertThat(task.duration() == null || task.duration().units() != null).isTrue();
        }
        for (RelationDTO relation : dto.relations()) {
            assertThat(relation.type()).isNotNull();
            assertThat(relation.predecessorId()).isNotNull();
            assertThat(relation.successorId()).isNotNull();
        }
    }

    @ParameterizedTest
    @MethodSource("corpus")
    void assignmentsNeverDangle(Path file) throws IOException {
        ProjectDTO dto = parser.parse(Files.readAllBytes(file));
        List<Integer> known = dto.resources().stream().map(r -> r.id()).toList();

        for (TaskDTO task : dto.tasks()) {
            for (var assignment : task.assignments()) {
                assertThat(known)
                        .as("assignment on task %s points at an unknown resource", task.id())
                        .contains(assignment.resourceId());
            }
        }
    }

    @Test
    void taskOrderIsDepthFirst() throws IOException {
        ProjectDTO dto = parser.parse(Files.readAllBytes(CORPUS.resolve("mpp14baseline.mpp")));

        List<Integer> seen = dto.tasks().stream().map(TaskDTO::id).toList();
        for (TaskDTO task : dto.tasks()) {
            if (task.parentId() != null) {
                assertThat(seen.indexOf(task.parentId()))
                        .as("parent of %s must appear before it", task.id())
                        .isLessThan(seen.indexOf(task.id()));
            }
        }
    }
}
