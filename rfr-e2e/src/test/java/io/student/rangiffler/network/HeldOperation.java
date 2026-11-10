package io.student.rangiffler.network;

import com.microsoft.playwright.Route;
import io.qameta.allure.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HeldOperation implements GqlOperation {
    private final String operation;
    private final List<Route> held;
    private final AtomicBoolean released;

    public HeldOperation(String operation) {
        this(operation, new ArrayList<>(), new AtomicBoolean());
    }

    HeldOperation(String operation, List<Route> held, AtomicBoolean released) {
        this.operation = operation;
        this.held = held;
        this.released = released;
    }

    @Override
    public void accept(Route route) {
        if (released.get() || !new GqlRequest(route.request()).matches(operation)) {
            route.fallback();
        } else {
            held.add(route);
        }
    }

    @Step("Отпустить удержанную GraphQL-операцию {this.operation}")
    public void release() {
        released.set(true);
        held.forEach(Route::fallback);
        held.clear();
    }
}
