package io.student.rangiffler.network;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.Jackson3JsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.Jackson3MappingProvider;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Route;

import java.util.Collection;
import java.util.Objects;

public final class PatchedOperation implements GqlOperation {
    private static final Configuration JSON_PATH = Configuration.builder()
            .jsonProvider(new Jackson3JsonNodeJsonProvider())
            .mappingProvider(new Jackson3MappingProvider())
            .options(Option.ALWAYS_RETURN_LIST)
            .build();

    private final String operation;
    private final String path;
    private final Object value;

    public PatchedOperation(String operation, String path, Object value) {
        this.operation = operation;
        this.path = path;
        this.value = value;
    }

    @Override
    public void accept(Route route) {
        if (!new GqlRequest(route.request()).matches(operation)) {
            route.fallback();
            return;
        }
        APIResponse response = route.fetch();
        String patched;
        try {
            patched = patched(response.text());
        } catch (RuntimeException failure) {
            route.abort();
            throw failure;
        }
        route.fulfill(new Route.FulfillOptions()
                .setResponse(response)
                .setBody(patched));
    }

    private String patched(String body) {
        DocumentContext document = JsonPath.using(JSON_PATH).parse(body);
        Object matched;
        try {
            matched = document.read(path);
        } catch (PathNotFoundException missing) {
            throw missingPath(missing);
        }
        if (matched instanceof Collection<?> values
                && (values.isEmpty() || values.stream().allMatch(Objects::isNull))) {
            throw missingPath(null);
        }
        try {
            return document.set(path, value).jsonString();
        } catch (PathNotFoundException missing) {
            throw missingPath(missing);
        }
    }

    private IllegalStateException missingPath(PathNotFoundException missing) {
        return new IllegalStateException(
                "JSON Path " + path + " for GraphQL operation " + operation + " matched no values",
                missing);
    }
}
