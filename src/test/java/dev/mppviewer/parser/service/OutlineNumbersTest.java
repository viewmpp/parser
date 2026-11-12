package dev.mppviewer.parser.service;

import dev.mppviewer.parser.model.dto.TaskDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class OutlineNumbersTest {

    private static TaskDTO task(int id, Integer parent, int level, String number) {
        return new TaskDTO(id, parent, "task " + id, number, number, level,
                null, null, null, 0.0, false, false, false, null, null, List.of());
    }

    private static List<String> numbers(List<TaskDTO> tasks) {
        return OutlineNumbers.repairIfRepeated(tasks).stream().map(TaskDTO::outlineNumber).toList();
    }

    @Test
    void numbersThatDoNotRepeatAreLeftAlone() {
        List<TaskDTO> tasks = List.of(task(1, null, 1, "1"), task(2, 1, 2, "1.1"), task(3, null, 1, "7"));

        assertThat(OutlineNumbers.repairIfRepeated(tasks)).isSameAs(tasks);
    }

    @Test
    void repeatedNumbersAreRebuiltFromTheTree() {
        List<TaskDTO> tasks = List.of(
                task(1, null, 1, "0"),
                task(2, 1, 2, "1"),
                task(3, 2, 3, "1"),
                task(4, 1, 2, "2"),
                task(5, null, 1, "0"));

        assertThat(numbers(tasks)).containsExactly("1", "1.1", "1.1.1", "1.2", "2");
    }

    @Test
    void theProjectSummaryRowStaysZeroAndItsChildrenCountFromOne() {
        List<TaskDTO> tasks = List.of(
                task(0, null, 0, "0"),
                task(1, 0, 1, "0"),
                task(2, 1, 2, "1"),
                task(3, 0, 1, "1"));

        assertThat(numbers(tasks)).containsExactly("0", "1", "1.1", "2");
    }

    @Test
    void aWBSThatOnlyCopiedTheBrokenNumberIsDropped() {
        TaskDTO copied = task(1, null, 1, "0");
        TaskDTO own = new TaskDTO(2, null, "task 2", "HOUSE.2", "0", 1,
                null, null, null, 0.0, false, false, false, null, null, List.of());

        List<TaskDTO> repaired = OutlineNumbers.repairIfRepeated(List.of(copied, own));

        assertThat(repaired.get(0).wbs()).isNull();
        assertThat(repaired.get(1).wbs()).isEqualTo("HOUSE.2");
    }
}
