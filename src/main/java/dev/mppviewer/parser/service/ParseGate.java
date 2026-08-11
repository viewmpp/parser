package dev.mppviewer.parser.service;

import dev.mppviewer.parser.exception.ParserBusyException;
import dev.mppviewer.parser.model.dto.ProjectDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.function.Supplier;


@Component
public class ParseGate {

    private final Semaphore slots;
    private final int size;

    public ParseGate(@Value("${parser.max-concurrent}") int size) {
        this.size = size;
        this.slots = new Semaphore(size);
    }

    public ProjectDTO run(Supplier<ProjectDTO> work) {
        if (!slots.tryAcquire()) {
            throw new ParserBusyException(size);
        }
        try {
            return work.get();
        } finally {
            slots.release();
        }
    }
}
