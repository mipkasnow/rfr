package io.student.rangiffler.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class TearDown {
    private final List<Runnable> steps;

    public TearDown() {
        this(List.of());
    }

    private TearDown(List<Runnable> steps) {
        this.steps = steps;
    }

    public TearDown with(Runnable step) {
        return new TearDown(Stream.concat(steps.stream(), Stream.of(step)).toList());
    }

    public <T> TearDown withEach(Iterable<T> items, Consumer<T> step) {
        TearDown result = this;
        for (T item : items) {
            result = result.with(() -> step.accept(item));
        }
        return result;
    }

    public void run() {
        List<RuntimeException> failures = new ArrayList<>();
        for (Runnable step : steps) {
            try {
                step.run();
            } catch (RuntimeException failure) {
                failures.add(failure);
            }
        }
        if (!failures.isEmpty()) {
            RuntimeException first = failures.getFirst();
            failures.stream().skip(1).forEach(first::addSuppressed);
            throw first;
        }
    }
}
