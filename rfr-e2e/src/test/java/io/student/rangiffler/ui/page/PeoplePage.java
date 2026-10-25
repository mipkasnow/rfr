package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.Elements;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PeoplePage {
    public static final String URL = TestConfig.frontUrl() + "/people";
    private final Page page;
    private final Elements elements;
    private final Locator allPeople;
    private final Locator friends;
    private final Locator incomeInvitations;
    private final Locator outcomeInvitations;
    private final Locator search;
    private final Locator rows;
    private final Locator peopleIcon;
    private final Locator noUsersYet;

    public PeoplePage(Page page) {
        this.page = page;
        this.elements = new Elements(page);
        this.allPeople = elements.byRole(AriaRole.TAB, "All People");
        this.friends = elements.byRole(AriaRole.TAB, "Friends");
        this.incomeInvitations = elements.byRole(AriaRole.TAB, "Income invitations");
        this.outcomeInvitations = elements.byRole(AriaRole.TAB, "Outcome invitations");
        this.search = elements.byRole(AriaRole.TEXTBOX, "search people");
        this.rows = elements.byRole(AriaRole.TABPANEL).getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHas(elements.byRole(AriaRole.ROWHEADER)));
        this.peopleIcon = elements.byTestId("PeopleOutlineOutlinedIcon");
        this.noUsersYet = elements.byText("There are no users yet");
    }

    @Step("Открыть вкладку All People")
    public PeoplePage clickAllPeople() {
        return openTab(allPeople);
    }

    @Step("Открыть вкладку Income invitations")
    public PeoplePage clickIncomeInvitations() {
        return openTab(incomeInvitations);
    }

    @Step("Проверить отсутствие входящих предложений дружить")
    public PeoplePage checkThatIncomeInvitationsListIsEmpty() {
        return checkThatListIsEmpty();
    }

    @Step("Открыть вкладку Friends")
    public PeoplePage clickFriends() {
        return openTab(friends);
    }

    @Step("Проверить отсутствие друзей")
    public PeoplePage checkThatFriendsListIsEmpty() {
        return checkThatListIsEmpty();
    }

    @Step("Открыть вкладку Outcome invitations")
    public PeoplePage clickOutcomeInvitations() {
        return openTab(outcomeInvitations);
    }

    @Step("Проверить отсутствие исходящих предложений дружить")
    public PeoplePage checkThatOutcomeInvitationsListIsEmpty() {
        return checkThatListIsEmpty();
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
                () -> button(username, "Add").click());
        return this;
    }

    @Step("Проверить, что в таблице ровно пользователи {usernames}")
    public PeoplePage checkThatPeople(List<String> usernames) {
        assertThat(rows).hasCount(usernames.size());
        usernames.forEach(username -> assertThat(row(username)).isVisible());
        return this;
    }

    @Step("Проверить, что у друга {username} есть кнопка Remove")
    public PeoplePage checkThatRemoveButton(String username) {
        assertThat(button(username, "Remove")).isVisible();
        return this;
    }

    @Step("Проверить, что у входящей заявки {username} есть кнопки Accept и Decline")
    public PeoplePage checkThatAcceptAndDeclineButtons(String username) {
        assertThat(button(username, "Accept")).isVisible();
        assertThat(button(username, "Decline")).isVisible();
        return this;
    }

    @Step("Проверить, что у исходящей заявки {username} есть Waiting... и кнопка Cancel")
    public PeoplePage checkThatWaitingAndCancel(String username) {
        assertThat(elements.byText(row(username), "Waiting...")).isVisible();
        assertThat(button(username, "Cancel")).isVisible();
        return this;
    }

    @Step("Проверить, что приглашение пользователю {username} ждёт ответа")
    public PeoplePage checkThatInvitationSent(String username) {
        assertThat(row(username)).containsText("Waiting...");
        return this;
    }

    private PeoplePage openTab(Locator tab) {
        tab.click();
        assertThat(tab).hasAttribute("aria-selected", "true");
        return this;
    }

    private PeoplePage checkThatListIsEmpty() {
        assertThat(peopleIcon).isVisible();
        assertThat(noUsersYet).isVisible();
        return this;
    }

    private Locator button(String username, String name) {
        return elements.byRole(row(username), AriaRole.BUTTON, name);
    }

    @Step("Найти строку пользователя {username}")
    private Locator row(String username) {
        return rows.filter(new Locator.FilterOptions().setHas(elements.byRole(AriaRole.CELL, username)));
    }
}
