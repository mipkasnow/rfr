package io.student.rangiffler.network;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public final class AwaitedGqlOperation {
    private final Page page;
    private final String operation;
    private final Runnable action;

    public AwaitedGqlOperation(Page page, String operation, Runnable action) {
        this.page = page;
        this.operation = operation;
        this.action = action;
    }

    @Step("Дождаться ответа на GraphQL-операцию {this.operation}")
    public void await() {
        page.waitForResponse(
                response -> new GqlRequest(response.request()).matches(operation),
                action
        );
    }
}
