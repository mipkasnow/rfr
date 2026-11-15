package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.domain.Notification;
import io.student.rangiffler.ui.component.PeopleTable;
import io.student.rangiffler.ui.component.Snackbar;

public final class PeoplePage {
    public static final String URL = TestConfig.frontUrl() + "/people";
    private final Page page;
    private final Locator search;
    private final PeopleTable table;
    private final Snackbar snackbar;

    public PeoplePage(Page page) {
        this.page = page;
        this.search = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("search people").setExact(true));
        this.table = new PeopleTable(page);
        this.snackbar = new Snackbar(page);
    }

    @Step("Открыть вкладку {tab}")
    public PeoplePage openTab(PeopleTab tab) {
        page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName(tab.title()).setExact(true)).click();
        return this;
    }

    @Step("Проверить уведомление на странице People: {notification}")
    public PeoplePage checkThatNotification(Notification notification) {
        snackbar.checkThatShown(notification);
        return this;
    }

    @Step("Проверить уведомление на странице People с текстом: {text}")
    public PeoplePage checkThatNotification(String text) {
        snackbar.checkThatShown(text);
        return this;
    }

    @Step("Проверить, что на странице People нет уведомления с текстом: {text}")
    public PeoplePage checkThatNotificationAbsent(String text) {
        snackbar.checkThatNotShown(text);
        return this;
    }

    @Step("Найти пользователя {username}")
    public PeoplePage searchPeople(String username) {
        search.fill(username);
        search.press("Enter");
        return this;
    }

    public PeopleTable table() {
        return table;
    }
}
