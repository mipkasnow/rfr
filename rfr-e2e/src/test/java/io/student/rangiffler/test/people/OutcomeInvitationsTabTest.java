package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.annotation.meta.WebTest;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.PeoplePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@WebTest
@DisplayName("Тесты исходящих запросов дружбы")
public class OutcomeInvitationsTabTest {

    @Test
    @DisplayName("Пользователь с 1 входящей заявкой и 1 другом не имеет исходящих заявок на вкладке Outcome invitations")
    void outcomeInvitationsEmptyList(Page page, @User(incomeInvitations = 1, friends = 1) TestUser user) {
        outcomeInvitations(page, user).checkThatOutcomeInvitationsListIsEmpty();
    }

    @Test
    @DisplayName("Пользователь с 1 другом и 1 исходящей заявкой видит Waiting... и кнопку Cancel на вкладке Outcome invitations")
    void outcomeInvitationsList(Page page, @User(friends = 1, outcomeInvitations = 1) TestUser user) {
        String invitee = user.outcomeInvitations().getFirst().username();
        outcomeInvitations(page, user)
                .checkThatPeople(List.of(invitee))
                .checkThatWaitingAndCancel(invitee);
    }

    private PeoplePage outcomeInvitations(Page page, TestUser user) {
        return new LandingPage(page).open()
                .clickLogin()
                .fillLoginForm(user.username(), user.password())
                .clickSignIn()
                .clickPeople()
                .clickOutcomeInvitations();
    }
}
