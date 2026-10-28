package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PeopleRow {
    private final Locator row;
    private final String username;

    public PeopleRow(Locator row, String username) {
        this.row = row;
        this.username = username;
    }

    @Step("Нажать Add у пользователя {this.username} и дождаться ответа API")
    public PeopleRow clickAdd() {
        row.page().waitForResponse(
                response -> response.url().endsWith("/graphql")
                        && String.valueOf(response.request().postData()).contains("FriendshipAction"),
                () -> row.getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Add").setExact(true)).click());
        return this;
    }

    @Step("Проверить, что приглашение пользователю {this.username} ждёт ответа")
    public PeopleRow checkThatWaiting() {
        assertThat(row).containsText("Waiting...");
        return this;
    }

    @Step("Проверить кнопки в строке пользователя {this.username}: {actions}")
    public PeopleRow checkThatActions(String... actions) {
        assertThat(row.getByRole(AriaRole.BUTTON)).hasText(actions);
        return this;
    }
}
