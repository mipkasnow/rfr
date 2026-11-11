package io.student.rangiffler.network;

import com.microsoft.playwright.BrowserContext;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

import java.util.ArrayList;
import java.util.List;

public final class GqlStubs implements AutoCloseable {
    private final BrowserContext context;
    private final List<GqlOperation> operations;

    public GqlStubs(BrowserContext context) {
        this(context, new ArrayList<>());
    }

    GqlStubs(BrowserContext context, List<GqlOperation> operations) {
        this.context = context;
        this.operations = operations;
    }

    public GqlStubs add(GqlOperation operation) {
        operations.add(operation);
        context.route(graphql(), operation);
        return this;
    }

    @Step("Удерживать GraphQL-операцию {operation}")
    public HeldOperation hold(String operation) {
        HeldOperation held = new HeldOperation(operation);
        add(held);
        return held;
    }

    @Step("Ответить на GraphQL-операцию {operation} телом из {resource}")
    public GqlStubs shouldReturnBody(String operation, String resource) {
        return add(new StubbedOperation(operation, resource));
    }

    @Step("Ответить на GraphQL-операцию {operation} ошибкой {type}: {text}")
    public GqlStubs error(String operation, GqlErrorType type, String text) {
        return add(new StubbedOperation(operation, () -> type.response(text)));
    }

    @Step("Подменить в ответе GraphQL-операции {operation} значение по пути {path} на {value}")
    public GqlStubs shouldPatchBody(String operation, String path, Object value) {
        return add(new PatchedOperation(operation, path, value));
    }

    @Override
    public void close() {
        operations.forEach(operation -> context.unroute(graphql(), operation));
    }

    private static String graphql() {
        return TestConfig.apiUrl() + "/graphql";
    }
}
