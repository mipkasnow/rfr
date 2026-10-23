package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PeoplePage {
    public static final String URL = TestConfig.frontUrl() + "/people";
    private final Page page;
    private final Locator allPeople;
    private final Locator incomeInvitations;
    private final Locator search;
    private final Locator rows;

    public PeoplePage(Page page) {
        this.page = page;
        this.allPeople = page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("All People").setExact(true));
        this.incomeInvitations = page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Income invitations").setExact(true));
        this.search = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("search people").setExact(true));
        this.rows = page.getByRole(AriaRole.TABPANEL).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.ROWHEADER)));
    }

    @Step("Открыть вкладку All People")
    public PeoplePage clickAllPeople() {
        allPeople.click();
        return this;
    }

    @Step("Открыть вкладку Income invitations")
    public PeoplePage clickIncomeInvitations() {
        incomeInvitations.click();
        return this;
    }

    @Step("Найти пользователя {username}")
    public PeoplePage searchPeople(String username) {
        search.fill(username);
        search.press("Enter");
        return this;
    }

    @Step("Нажать Add у пользователя {username} и дождаться ответа API")
    public PeoplePage clickAdd(String username) {
        page.waitForResponse(
                response -> response.url().endsWith("/graphql")
                        && String.valueOf(response.request().postData()).contains("FriendshipAction"),
                () -> row(username).getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Add").setExact(true)).click());
        return this;
    }

    @Step("Проверить, что в таблице ровно пользователи {usernames}")
    public PeoplePage checkThatPeople(List<String> usernames) {
        assertThat(rows).hasCount(usernames.size());
        usernames.forEach(username -> assertThat(row(username)).isVisible());
        return this;
    }

    @Step("Проверить, что приглашение пользователю {username} ждёт ответа")
    public PeoplePage checkThatInvitationSent(String username) {
        assertThat(row(username)).containsText("Waiting...");
        return this;
    }

    @Step("Найти строку пользователя {username}")
    private Locator row(String username) {
        return rows.filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.CELL,
                new Page.GetByRoleOptions().setName(username).setExact(true))));
    }
}
