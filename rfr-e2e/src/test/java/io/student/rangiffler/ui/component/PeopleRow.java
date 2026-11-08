package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import java.util.Arrays;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PeopleRow {
    private final Locator row;
    private final String username;

    public PeopleRow(Locator row, String username) {
        this.row = row;
        this.username = username;
    }

    @Step("Нажать Add у пользователя {this.username}")
    public PeopleRow clickAdd() {
        button(PeopleAction.ADD).click();
        return this;
    }

    @Step("Нажать Accept у пользователя {this.username}")
    public PeopleRow clickAccept() {
        button(PeopleAction.ACCEPT).click();
        return this;
    }

    @Step("Нажать Decline у пользователя {this.username}")
    public PeopleRow clickDecline() {
        button(PeopleAction.DECLINE).click();
        return this;
    }

    @Step("Нажать Remove у пользователя {this.username}")
    public PeopleRow clickRemove() {
        button(PeopleAction.REMOVE).click();
        return this;
    }

    @Step("Проверить, что приглашение пользователю {this.username} ждёт ответа")
    public PeopleRow checkThatWaiting() {
        assertThat(row).containsText("Waiting...");
        return this;
    }

    @Step("Проверить кнопки в строке пользователя {this.username}: {actions}")
    public PeopleRow checkThatActions(PeopleAction... actions) {
        assertThat(row.getByRole(AriaRole.BUTTON))
                .hasText(Arrays.stream(actions).map(PeopleAction::title).toArray(String[]::new));
        return this;
    }

    private Locator button(PeopleAction action) {
        return row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(action.title()).setExact(true));
    }
}
