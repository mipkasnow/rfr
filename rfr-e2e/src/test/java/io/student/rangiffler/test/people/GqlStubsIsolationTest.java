package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.domain.FriendStatus;
import io.student.rangiffler.jupiter.annotation.Session;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.network.AwaitedGqlOperation;
import io.student.rangiffler.network.GqlStubs;
import io.student.rangiffler.session.UserSession;
import io.student.rangiffler.ui.page.PeoplePage;
import io.student.rangiffler.ui.page.PeopleTab;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("Изоляция GraphQL-стабов двух контекстов")
class GqlStubsIsolationTest {
    private static final String STUBBED_PERSON = "stubbed-person";
    private static final String STUBBED_PEOPLE = "gql/get-people-stubbed.json";

    @Test
    @DisplayName("Стаб сессии подменяет GetPeople только во втором контексте")
    void sessionStubDoesNotAffectMainContext(Page page,
                                             @User TestUser alice,
                                             @Session(useStubs = true) @User UserSession bob) {
        bob.stubs().orElseThrow().shouldReturnBody("GetPeople", STUBBED_PEOPLE);

        PeoplePage main = PeopleLogin.open(page, alice)
                .openTab(PeopleTab.ALL_PEOPLE);
        new AwaitedGqlOperation(page, "GetPeople", () -> main.searchPeople(bob.user().username())).await();
        main.table()
                .checkThatPeople(List.of(bob.user().username()))
                .row(bob.user().username())
                .checkThatStatus(FriendStatus.NOT_FRIEND);

        bob.mainPage()
                .clickPeople()
                .openTab(PeopleTab.ALL_PEOPLE)
                .table()
                .checkThatPeople(List.of(STUBBED_PERSON))
                .row(STUBBED_PERSON)
                .checkThatStatus(FriendStatus.FRIEND);
    }

    @Test
    @DisplayName("Стаб основного контекста подменяет GetPeople только в нём")
    void mainStubDoesNotAffectSession(Page page,
                                      GqlStubs stubs,
                                      @User TestUser alice,
                                      @Session(useStubs = true) @User UserSession bob) {
        stubs.shouldReturnBody("GetPeople", STUBBED_PEOPLE);

        PeoplePage main = PeopleLogin.open(page, alice);
        main.openTab(PeopleTab.ALL_PEOPLE)
                .table()
                .checkThatPeople(List.of(STUBBED_PERSON))
                .row(STUBBED_PERSON)
                .checkThatStatus(FriendStatus.FRIEND);

        PeoplePage session = bob.mainPage().clickPeople();
        session.openTab(PeopleTab.ALL_PEOPLE)
                .searchPeople(alice.username())
                .table()
                .checkThatPeople(List.of(alice.username()))
                .row(alice.username())
                .checkThatStatus(FriendStatus.NOT_FRIEND);
    }
}
