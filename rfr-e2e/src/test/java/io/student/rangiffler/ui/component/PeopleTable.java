package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PeopleTable {
    private final Page page;
    private final Locator panel;
    private final Locator rows;

    public PeopleTable(Page page) {
        this.page = page;
        this.panel = page.getByRole(AriaRole.TABPANEL);
        this.rows = panel.getByTestId("people-row");
    }

    public PeopleRow row(String username) {
        return new PeopleRow(rowOf(username), username);
    }

    @Step("Проверить, что в таблице ровно пользователи {usernames}")
    public PeopleTable checkThatPeople(List<String> usernames) {
        assertThat(rows).hasCount(usernames.size());
        usernames.forEach(username -> assertThat(rowOf(username)).isVisible());
        return this;
    }

    @Step("Проверить, что на вкладке нет пользователей")
    public PeopleTable checkThatEmpty() {
        assertThat(panel.getByText("There are no users yet",
                new Locator.GetByTextOptions().setExact(true))).isVisible();
        return this;
    }

    private Locator rowOf(String username) {
        return rows.filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.CELL,
                new Page.GetByRoleOptions().setName(username).setExact(true))));
    }
}
