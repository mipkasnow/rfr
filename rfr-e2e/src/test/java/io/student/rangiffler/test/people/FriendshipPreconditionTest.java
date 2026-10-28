package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.PeoplePage;
import io.student.rangiffler.ui.page.PeopleTab;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("Предусловия дружбы")
class FriendshipPreconditionTest {
    @Test
    @DisplayName("Пользователь с двумя друзьями видит их на вкладке Friends")
    void friendsPreconditionIsVisible(Page page, @User(friends = 2) TestUser user) {
        people(page, user)
                .table()
                .checkThatPeople(usernames(user.friends()));
    }

    @Test
    @DisplayName("Пользователь с одной заявкой видит её на вкладке Income invitations")
    void incomeInvitationPreconditionIsVisible(Page page, @User(incomeInvitations = 1) TestUser user) {
        people(page, user)
                .openTab(PeopleTab.INCOME_INVITATIONS)
                .table()
                .checkThatPeople(usernames(user.incomeInvitations()));
    }

    @Test
    @DisplayName("Друзья и заявки создаются вместе цепочкой декораторов")
    void chainedPreconditionsAreVisible(Page page, @User(friends = 2, incomeInvitations = 3) TestUser user) {
        PeoplePage people = people(page, user);
        people.table().checkThatPeople(usernames(user.friends()));
        people.openTab(PeopleTab.INCOME_INVITATIONS)
                .table()
                .checkThatPeople(usernames(user.incomeInvitations()));
    }

    private PeoplePage people(Page page, TestUser user) {
        return PeopleLogin.open(page, user);
    }

    private List<String> usernames(List<TestUser> users) {
        return users.stream().map(TestUser::username).toList();
    }
}
