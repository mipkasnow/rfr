package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.PeopleTable;

public final class PeoplePage {
    public static final String URL = TestConfig.frontUrl() + "/people";
    private final Page page;
    private final Locator search;
    private final PeopleTable table;

    public PeoplePage(Page page) {
        this.page = page;
        this.search = page.getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("search people").setExact(true));
        this.table = new PeopleTable(page);
    }

    @Step("Открыть вкладку {tab.title}")
    public PeoplePage openTab(PeopleTab tab) {
        page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Broken " + tab.title()).setExact(true)).click();
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
