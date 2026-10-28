package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.config.TestConfig;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtensionContext;

public final class StandReadinessExtension implements SuiteExtension {

    private static final Duration POLL_INTERVAL = Duration.ofSeconds(2);

    @Override
    public void beforeSuite(ExtensionContext context) {
        if (!TestConfig.standCheck()) {
            return;
        }
        Duration budget = Duration.ofSeconds(TestConfig.standTimeout());
        long deadline = System.nanoTime() + budget.toNanos();
        Set<StandService> pending = EnumSet.allOf(StandService.class);
        while (!pending.isEmpty()) {
            for (StandService service : List.copyOf(pending)) {
                if (service.ready()) {
                    pending.remove(service);
                }
            }
            if (pending.isEmpty()) {
                return;
            }
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0) {
                throw new IllegalStateException(timeoutMessage(pending, budget));
            }
            try {
                Thread.sleep(Math.min(POLL_INTERVAL.toMillis(), Math.max(1L, remainingNanos / 1_000_000L)));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for stand readiness", exception);
            }
        }
    }

    private static String timeoutMessage(Set<StandService> pending, Duration budget) {
        String services = pending.stream()
                .map(service -> service.serviceName() + " (" + service.startCommand() + ")")
                .collect(Collectors.joining(", "));
        return "Stand is not ready within " + budget.toSeconds() + "s: " + services;
    }
}
