package io.student.rangiffler.network;

import com.microsoft.playwright.Request;
import io.student.rangiffler.config.TestConfig;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

final class GqlRequest {
    private final Request request;

    GqlRequest(Request request) {
        this.request = request;
    }

    boolean matches(String operation) {
        return graphql() && operationName().equals(operation);
    }

    String operationName() {
        return body().required("operationName").asString();
    }

    JsonNode variables() {
        return body().required("variables");
    }

    private boolean graphql() {
        return "POST".equals(request.method()) && request.url().equals(TestConfig.apiUrl() + "/graphql");
    }

    private JsonNode body() {
        if (!graphql()) {
            throw new IllegalStateException("Request " + request.method() + " " + request.url() + " is not a GraphQL operation");
        }
        return JsonMapper.shared().readTree(request.postData());
    }
}
