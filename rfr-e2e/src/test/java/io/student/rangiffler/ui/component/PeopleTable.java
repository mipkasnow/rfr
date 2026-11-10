package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public final class PeopleTable {
    private final Page page;
    private final Locator panel;
    private final Locator rows;
    private final Locator loader;

    public PeopleTable(Page page) {
        this.page = page;
        this.panel = page.getByRole(AriaRole.TABPANEL);
        this.rows = panel.getByTestId("people-row");
        this.loader = panel.getByRole(AriaRole.PROGRESSBAR,
                new Locator.GetByRoleOptions().setName("Loading users").setExact(true));
    }

    public PeopleRow row(String username) {
        return new PeopleRow(rowOf(username), username);
    }

    @Step("Проверить, что в таблице ровно пользователи {usernames}")
    public PeopleTable checkThatPeople(List<String> usernames) {
        assertAll("Таблица People показывает не ровно пользователей " + usernames,
                Stream.concat(
                        Stream.<Executable>of(() -> assertThat(rows).hasCount(usernames.size())),
                        usernames.stream().<Executable>map(username -> () -> assertThat(rowOf(username)).isVisible())
                ));
        return this;
    }

    @Step("Проверить, что на вкладке нет пользователей")
    public PeopleTable checkThatEmpty() {
        assertThat(panel.getByText("There are no users yet",
                new Locator.GetByTextOptions().setExact(true))).isVisible();
        return this;
    }

    @Step("Проверить, что таблица показывает загрузку пользователей")
    public PeopleTable checkThatLoaderShown() {
        assertThat(loader).isVisible();
        return this;
    }

    @Step("Проверить, что загрузка пользователей в таблице закончилась")
    public PeopleTable checkThatLoaderHidden() {
        assertThat(loader).hasCount(0);
        return this;
    }

    private Locator rowOf(String username) {
        return rows.filter(new Locator.FilterOptions().setHas(page.getByText(username,
                new Page.GetByTextOptions().setExact(true))));
    }
}
