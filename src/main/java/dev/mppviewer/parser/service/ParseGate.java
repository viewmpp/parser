package dev.mppviewer.parser.service;

import dev.mppviewer.parser.exception.ParserBusyException;
import dev.mppviewer.parser.model.dto.ProjectDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.function.Supplier;


/**
 * Разбор одного файла занимает памяти на порядок больше самого файла: замер на
 * .mpp в 2.6 МБ дал прирост кучи около 57 МБ. Лимит на размер тела ограничивает
 * один запрос, но не их сумму, поэтому предел одновременных разборов — это
 * единственное, что делает потребление памяти предсказуемым.
 *
 * Слот удерживается до конца работы намеренно. Отпустить его по таймауту нельзя:
 * MPXJ игнорирует прерывание потока (проверено), работа продолжилась бы, а место
 * заняла бы следующая — и гарантия по памяти исчезла бы.
 */
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
