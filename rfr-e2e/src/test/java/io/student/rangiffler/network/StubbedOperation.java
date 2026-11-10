package io.student.rangiffler.network;

import com.microsoft.playwright.Route;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public final class StubbedOperation implements GqlOperation {
    private final String operation;
    private final Supplier<String> body;

    public StubbedOperation(String operation, String resource) {
        this(operation, () -> resource(operation, resource));
    }

    public StubbedOperation(String operation, Supplier<String> body) {
        this.operation = operation;
        this.body = body;
    }

    @Override
    public void accept(Route route) {
        if (new GqlRequest(route.request()).matches(operation)) {
            route.fulfill(new Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody(body.get()));
        } else {
            route.fallback();
        }
    }

    private static String resource(String operation, String resource) {
        try (InputStream stream = StubbedOperation.class.getResourceAsStream("/" + resource)) {
            if (stream == null) {
                throw new IllegalStateException("Stub " + resource + " for GraphQL operation " + operation + " is not on the test classpath");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException("Stub " + resource + " for GraphQL operation " + operation + " was not read", failure);
        }
    }
}
