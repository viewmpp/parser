package dev.mppviewer.parser.service;

import dev.mppviewer.parser.model.dto.TaskDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


final class OutlineNumbers {

    private static final String PROJECT_ROW = "0";

    private OutlineNumbers() {
    }

    static List<TaskDTO> repairIfRepeated(List<TaskDTO> tasks) {
        if (!repeated(tasks)) {
            return tasks;
        }

        Map<Integer, String> numbers = new HashMap<>();
        Map<Integer, Integer> children = new HashMap<>();
        List<TaskDTO> out = new ArrayList<>(tasks.size());

        for (TaskDTO task : tasks) {
            String number = number(task, numbers, children);
            numbers.put(task.id(), number);

            String wbs = Objects.equals(task.wbs(), task.outlineNumber()) ? null : task.wbs();
            out.add(withNumbers(task, wbs, number));
        }

        return out;
    }

    private static boolean repeated(List<TaskDTO> tasks) {
        Set<String> seen = new HashSet<>();
        for (TaskDTO task : tasks) {
            if (task.outlineNumber() != null && !seen.add(task.outlineNumber())) {
                return true;
            }
        }
        return false;
    }

    private static String number(TaskDTO task, Map<Integer, String> numbers, Map<Integer, Integer> children) {
        Integer level = task.outlineLevel();
        if (task.parentId() == null && level != null && level == 0) {
            return PROJECT_ROW;
        }

        int position = children.merge(task.parentId(), 1, Integer::sum);
        String parent = task.parentId() == null ? null : numbers.get(task.parentId());

        if (parent == null || parent.equals(PROJECT_ROW)) {
            return Integer.toString(position);
        }
        return parent + "." + position;
    }

    private static TaskDTO withNumbers(TaskDTO task, String wbs, String outlineNumber) {
        return new TaskDTO(
                task.id(),
                task.parentId(),
                task.name(),
                wbs,
                outlineNumber,
                task.outlineLevel(),
                task.start(),
                task.finish(),
                task.duration(),
                task.percentComplete(),
                task.isSummary(),
                task.isMilestone(),
                task.isCritical(),
                task.notes(),
                task.baseline(),
                task.assignments()
        );
    }
}
