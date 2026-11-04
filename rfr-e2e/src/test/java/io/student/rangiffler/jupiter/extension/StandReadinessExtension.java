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
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (!TestConfig.standCheck()) {
            return;
        }
        if (!context.getTags().contains("ui")) {
            return;
        }
        SuiteExtension.super.beforeAll(context);
    }

    @Override
    public void beforeSuite(ExtensionContext context) {
        Duration budget = Duration.ofSeconds(TestConfig.standTimeout());
        long deadline = System.nanoTime() + budget.toNanos();
        Set<StandService> pending = EnumSet.allOf(StandService.class);
        while (!pending.isEmpty()) {
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0) {
                throw new IllegalStateException(timeoutMessage(pending, budget));
            }
            Duration requestTimeout = REQUEST_TIMEOUT.compareTo(Duration.ofNanos(remainingNanos)) <= 0
                    ? REQUEST_TIMEOUT
                    : Duration.ofNanos(remainingNanos);
            for (StandService service : List.copyOf(pending)) {
                if (service.ready(requestTimeout)) {
                    pending.remove(service);
                }
            }
            if (pending.isEmpty()) {
                return;
            }
            remainingNanos = deadline - System.nanoTime();
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
