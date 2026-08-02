package dev.mppviewer.parser;

import dev.mppviewer.parser.exception.ParserBusyException;
import dev.mppviewer.parser.model.dto.ProjectDTO;
import dev.mppviewer.parser.service.ParseGate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ParseGateTest {

    private static final ProjectDTO EMPTY =
            new ProjectDTO(1, null, null, List.of(), List.of(), List.of());

    @Test
    void rejectsBeyondCapacity() throws Exception {
        ParseGate gate = new ParseGate(2);
        CountDownLatch occupied = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);

        for (int i = 0; i < 2; i++) {
            Thread.ofPlatform().start(() -> gate.run(() -> {
                occupied.countDown();
                await(release);
                return EMPTY;
            }));
        }

        assertThat(occupied.await(5, TimeUnit.SECONDS)).isTrue();

        assertThatThrownBy(() -> gate.run(() -> EMPTY))
                .isInstanceOf(ParserBusyException.class);

        release.countDown();
    }

    @Test
    void releasesSlotAfterFailure() {
        ParseGate gate = new ParseGate(1);

        assertThatThrownBy(() -> gate.run(() -> { throw new IllegalStateException("boom"); }))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gate.run(() -> EMPTY)).isSameAs(EMPTY);
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
