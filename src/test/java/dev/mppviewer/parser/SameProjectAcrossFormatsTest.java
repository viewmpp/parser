package dev.mppviewer.parser;

import dev.mppviewer.parser.model.dto.ProjectDTO;
import dev.mppviewer.parser.model.dto.RelationDTO;
import dev.mppviewer.parser.model.dto.TaskDTO;
import dev.mppviewer.parser.service.ProjectParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class SameProjectAcrossFormatsTest {

    @Autowired
    private ProjectParser parser;

    private ProjectDTO parse(String file) throws IOException {
        return parser.parse(Files.readAllBytes(Path.of("testdata", file)));
    }

    @Test
    void mppVersionsAgree() throws IOException {
        ProjectDTO current = parse("real/large.mpp");
        ProjectDTO legacy = parse("real/large2007.mpp");

        assertThat(legacy.tasks()).hasSameSizeAs(current.tasks());
        assertThat(legacy.relations()).hasSameSizeAs(current.relations());
        assertThat(names(legacy)).isEqualTo(names(current));
        assertThat(links(legacy)).isEqualTo(links(current));
    }

    @Test
    void mppCarriesWhatSyntheticMspdiLacks() throws IOException {
        ProjectDTO mpp = parse("real/large.mpp");
        ProjectDTO mspdi = parse("large_1500.xml");

        assertThat(mspdi.tasks()).hasSize(mpp.tasks().size() - 1);
        assertThat(links(mpp)).isEqualTo(links(mspdi));

        assertThat(undatedSummaries(mpp)).isZero();
        assertThat(undatedSummaries(mspdi)).isPositive();

        assertThat(critical(mpp)).isPositive();
        assertThat(critical(mspdi)).isZero();
    }

    private static List<String> names(ProjectDTO dto) {
        return dto.tasks().stream().map(TaskDTO::name).toList();
    }

    private static Set<String> links(ProjectDTO dto) {
        return dto.relations().stream()
                .map(r -> r.predecessorId() + "->" + r.successorId() + ":" + r.type())
                .collect(Collectors.toSet());
    }

    private static long undatedSummaries(ProjectDTO dto) {
        return dto.tasks().stream().filter(TaskDTO::isSummary)
                .filter(t -> t.start() == null || t.finish() == null).count();
    }

    private static long critical(ProjectDTO dto) {
        return dto.tasks().stream().filter(TaskDTO::isCritical).count();
    }
}
