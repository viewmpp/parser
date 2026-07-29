package dev.mppviewer.parser.service;

import dev.mppviewer.parser.exception.InvalidProjectFileException;
import dev.mppviewer.parser.exception.ParseError;
import dev.mppviewer.parser.model.Contract;
import dev.mppviewer.parser.model.dto.AssignmentDTO;
import dev.mppviewer.parser.model.dto.BaselineDTO;
import dev.mppviewer.parser.model.dto.CalendarDTO;
import dev.mppviewer.parser.model.dto.CalendarExceptionDTO;
import dev.mppviewer.parser.model.dto.DurationDTO;
import dev.mppviewer.parser.model.dto.ProjectDTO;
import dev.mppviewer.parser.model.dto.ProjectInfoDTO;
import dev.mppviewer.parser.model.dto.RelationDTO;
import dev.mppviewer.parser.model.dto.ResourceDTO;
import dev.mppviewer.parser.model.dto.TaskDTO;
import org.jspecify.annotations.NonNull;
import org.mpxj.DayType;
import org.mpxj.Duration;
import org.mpxj.MPXJException;
import org.mpxj.ProjectCalendar;
import org.mpxj.ProjectCalendarException;
import org.mpxj.ProjectFile;
import org.mpxj.Relation;
import org.mpxj.Resource;
import org.mpxj.ResourceAssignment;
import org.mpxj.Task;
import org.mpxj.reader.UniversalProjectReader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class MpxjProjectParser implements ProjectParser {

    @Override
    public ProjectDTO parse(byte[] source) {
        ProjectFile project = read(source);

        List<TaskDTO> tasks = new ArrayList<>();
        collectTasks(project.getChildTasks(), tasks);

        return new ProjectDTO(
                Contract.VERSION,
                projectInfo(project),
                calendar(project),
                resources(project),
                tasks,
                relations(project)
        );
    }

    private ProjectFile read(byte[] source) {
        ProjectFile project;
        try {
            project = new UniversalProjectReader().read(new ByteArrayInputStream(source));
        } catch (MPXJException | RuntimeException e) {
            throw new InvalidProjectFileException(ParseError.CORRUPT_FILE, e.getMessage(), e);
        }
        if (project == null) {
            throw new InvalidProjectFileException(ParseError.UNSUPPORTED_FORMAT, "reader returned null", null);
        }
        return project;
    }

    private void collectTasks(List<Task> nodes, List<TaskDTO> out) {
        for (Task task : nodes) {
            out.add(task(task));
            collectTasks(task.getChildTasks(), out);
        }
    }

    private TaskDTO task(Task task) {
        return new TaskDTO(
                task.getUniqueID(),
                task.getParentTaskUniqueID(),
                task.getName(),
                emptyToNull(task.getWBS()),
                emptyToNull(task.getOutlineNumber()),
                task.getOutlineLevel(),
                task.getStart(),
                task.getFinish(),
                duration(task.getDuration()),
                percent(task.getPercentageComplete()),
                task.getSummary(),
                task.getMilestone(),
                task.getCritical(),
                emptyToNull(task.getNotes()),
                baseline(task),
                assignments(task)
        );
    }

    private BaselineDTO baseline(Task task) {
        if (task.getBaselineStart() == null || task.getBaselineFinish() == null) {
            return null;
        }
        return new BaselineDTO(task.getBaselineStart(), task.getBaselineFinish());
    }

    private List<AssignmentDTO> assignments(Task task) {
        List<AssignmentDTO> out = new ArrayList<>();
        for (ResourceAssignment assignment : task.getResourceAssignments()) {
            if (assignment.getResourceUniqueID() == null) {
                continue;
            }
            out.add(new AssignmentDTO(assignment.getResourceUniqueID(), assignment.getUnits()));
        }
        return out;
    }

    private List<RelationDTO> relations(ProjectFile project) {
        List<RelationDTO> out = new ArrayList<>();
        int id = 1;
        for (Task task : project.getTasks()) {
            List<Relation> predecessors = task.getPredecessors();
            if (predecessors == null) {
                continue;
            }
            for (Relation relation : predecessors) {
                Task predecessor = relation.getPredecessorTask();
                if (predecessor == null) {
                    continue;
                }
                out.add(new RelationDTO(
                        id++,
                        predecessor.getUniqueID(),
                        task.getUniqueID(),
                        relation.getType() == null ? null : relation.getType().name(),
                        duration(relation.getLag())
                ));
            }
        }
        return out;
    }

    private List<ResourceDTO> resources(ProjectFile project) {
        List<ResourceDTO> out = new ArrayList<>();
        for (Resource resource : project.getResources()) {
            if (resource.getUniqueID() == null || emptyToNull(resource.getName()) == null) {
                continue;
            }
            out.add(new ResourceDTO(resource.getUniqueID(), resource.getName()));
        }
        return out;
    }

    private double percent(Number value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private ProjectInfoDTO projectInfo(ProjectFile project) {
        return new ProjectInfoDTO(
                emptyToNull(project.getProjectProperties().getName()),
                project.getProjectProperties().getStartDate(),
                project.getProjectProperties().getFinishDate()
        );
    }

    private CalendarDTO calendar(ProjectFile project) {
        ProjectCalendar calendar = project.getDefaultCalendar();
        if (calendar == null) {
            return new CalendarDTO(null, List.of(), List.of());
        }

        List<String> nonWorking = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            if (calendar.getCalendarDayType(day) == DayType.NON_WORKING) {
                nonWorking.add(day.name());
            }
        }

        List<CalendarExceptionDTO> exceptions = getCalendarExceptionDTOS(calendar);

        return new CalendarDTO(emptyToNull(calendar.getName()), nonWorking, exceptions);
    }

    private @NonNull List<CalendarExceptionDTO> getCalendarExceptionDTOS(ProjectCalendar calendar) {
        List<CalendarExceptionDTO> exceptions = new ArrayList<>();
        for (ProjectCalendarException exception : calendar.getCalendarExceptions()) {
            LocalDate from = exception.getFromDate();
            if (from == null) {
                continue;
            }
            LocalDate to = exception.getToDate() == null ? from : exception.getToDate();
            exceptions.add(new CalendarExceptionDTO(
                    from.atStartOfDay(),
                    to.atStartOfDay(),
                    exception.getWorking(),
                    emptyToNull(exception.getName())
            ));
        }
        return exceptions;
    }

    private DurationDTO duration(Duration duration) {
        if (duration == null || duration.getUnits() == null) {
            return null;
        }
        return new DurationDTO(duration.getDuration(), duration.getUnits().name());
    }

    private String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}
