package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.domain.FriendStatus;
import io.student.rangiffler.domain.PeopleAction;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public final class PeopleRow {
    private final Locator row;
    private final String username;
    private final Locator waiting;

    public PeopleRow(Locator row, String username) {
        this.row = row;
        this.username = username;
        this.waiting = row.getByText("Waiting...", new Locator.GetByTextOptions().setExact(true));
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

    @Step("Нажать Cancel у пользователя {this.username}")
    public PeopleRow clickCancel() {
        button(PeopleAction.CANCEL).click();
        return this;
    }

    @Step("Нажать Remove у пользователя {this.username}")
    public PeopleRow clickRemove() {
        button(PeopleAction.REMOVE).click();
        return this;
    }

    @Step("Проверить, что пользователь {this.username} в статусе {status}")
    public PeopleRow checkThatStatus(FriendStatus status) {
        assertAll("Строка пользователя " + username + " не соответствует статусу " + status,
                () -> assertThat(row.getByRole(AriaRole.BUTTON))
                        .hasText(status.actions().stream().map(PeopleAction::title).toArray(String[]::new)),
                () -> assertThat(waiting).hasCount(status.waiting() ? 1 : 0));
        return this;
    }

    private Locator button(PeopleAction action) {
        return row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(action.title()).setExact(true));
    }
}
