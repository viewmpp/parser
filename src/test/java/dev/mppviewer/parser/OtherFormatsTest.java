package dev.mppviewer.parser;

import dev.mppviewer.parser.exception.InvalidProjectFileException;
import dev.mppviewer.parser.model.dto.ProjectDTO;
import dev.mppviewer.parser.model.dto.RelationDTO;
import dev.mppviewer.parser.model.dto.ResourceDTO;
import dev.mppviewer.parser.model.dto.TaskDTO;
import dev.mppviewer.parser.service.ProjectParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mpxj.ProjectFile;
import org.mpxj.Task;
import org.mpxj.reader.UniversalProjectReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
class OtherFormatsTest {

    private static final Path FORMATS = Path.of("testdata", "formats");
    private static final String PROJECT_98_DATABASE = "Xtask-links-project98-mpd8.mpd";

    @Autowired
    private ProjectParser parser;

    static Stream<Path> readable() throws IOException {
        try (var files = Files.list(FORMATS)) {
            return files.filter(p -> !p.getFileName().toString().equals(PROJECT_98_DATABASE))
                    .sorted()
                    .toList()
                    .stream();
        }
    }

    @ParameterizedTest
    @MethodSource("readable")
    void everyFormatTheSitePromisesReadsIntoAPlan(Path file) throws IOException {
        ProjectDTO dto = parser.parse(Files.readAllBytes(file));

        assertThat(dto.contractVersion()).isEqualTo(1);
        assertThat(dto.tasks()).as("tasks in %s", file.getFileName()).isNotEmpty();
        assertThat(dto.tasks())
                .as("%s reads, but no task has both dates, so the chart would be empty", file.getFileName())
                .anyMatch(t -> t.start() != null && t.finish() != null);
    }

    @ParameterizedTest
    @MethodSource("readable")
    void nothingInAPlanPointsAtATaskOrResourceItDoesNotHave(Path file) throws IOException {
        ProjectDTO dto = parser.parse(Files.readAllBytes(file));

        Set<Integer> tasks = dto.tasks().stream().map(TaskDTO::id).collect(Collectors.toSet());
        Set<Integer> resources = dto.resources().stream().map(ResourceDTO::id).collect(Collectors.toSet());

        for (TaskDTO task : dto.tasks()) {
            if (task.parentId() != null) {
                assertThat(tasks).as("parent of task %s", task.id()).contains(task.parentId());
            }
            for (var assignment : task.assignments()) {
                assertThat(resources).as("resource assigned to task %s", task.id()).contains(assignment.resourceId());
            }
        }
        for (RelationDTO relation : dto.relations()) {
            assertThat(tasks).as("predecessor of relation %s", relation.id()).contains(relation.predecessorId());
            assertThat(tasks).as("successor of relation %s", relation.id()).contains(relation.successorId());
        }
    }

    @Test
    void ganttProjectBringsItsHierarchyLinksAndPeople() throws IOException {
        ProjectDTO dto = parser.parse(Files.readAllBytes(FORMATS.resolve("HouseBuildingSample.gan")));

        assertThat(dto.tasks()).hasSize(20);
        assertThat(dto.tasks()).filteredOn(TaskDTO::isSummary).isNotEmpty();
        assertThat(dto.relations()).isNotEmpty();
        assertThat(dto.resources()).isNotEmpty();
        assertThat(dto.tasks()).anyMatch(t -> !t.assignments().isEmpty());
    }

    @Test
    void ganttProjectGetsOutlineNumbersFromItsHierarchy() throws IOException {
        ProjectDTO dto = parser.parse(Files.readAllBytes(FORMATS.resolve("HouseBuildingSample.gan")));

        List<String> numbers = dto.tasks().stream().map(TaskDTO::outlineNumber).toList();

        assertThat(numbers.subList(0, 9)).containsExactly("1", "1.1", "1.2", "1.3", "2", "2.1", "2.2", "2.3", "3");
        assertThat(numbers).doesNotHaveDuplicates();
        assertThat(dto.tasks())
                .as("MPXJ copies the same per-level index into the WBS, which numbers nothing")
                .allMatch(t -> t.wbs() == null);
    }

    static Stream<Path> keepTheirOwnNumbers() throws IOException {
        Stream<Path> formats = readable().filter(p -> !p.getFileName().toString().endsWith(".gan"));
        try (var real = Files.list(Path.of("testdata", "real"))) {
            return Stream.concat(formats, real.filter(p -> p.toString().endsWith(".mpp")).sorted().toList().stream());
        }
    }

    @ParameterizedTest
    @MethodSource("keepTheirOwnNumbers")
    void aFileWhoseNumbersDoNotRepeatKeepsThem(Path file) throws Exception {
        ProjectDTO dto = parser.parse(Files.readAllBytes(file));
        ProjectFile project = new UniversalProjectReader().read(file.toFile());

        Map<Integer, String> outline = new HashMap<>();
        Map<Integer, String> wbs = new HashMap<>();
        for (Task task : project.getTasks()) {
            outline.put(task.getUniqueID(), blankToNull(task.getOutlineNumber()));
            wbs.put(task.getUniqueID(), blankToNull(task.getWBS()));
        }

        for (TaskDTO task : dto.tasks()) {
            assertThat(task.outlineNumber()).as("outline number of task %s", task.id()).isEqualTo(outline.get(task.id()));
            assertThat(task.wbs()).as("WBS of task %s", task.id()).isEqualTo(wbs.get(task.id()));
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    @Test
    void aProject98DatabaseIsNotRecognised() throws IOException {
        byte[] source = Files.readAllBytes(FORMATS.resolve(PROJECT_98_DATABASE));

        assertThatThrownBy(() -> parser.parse(source)).isInstanceOf(InvalidProjectFileException.class);
    }
}
