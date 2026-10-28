package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.PeopleTab;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("Вкладка Income invitations")
class IncomeInvitationsTabTest {
    @Test
    @DisplayName("Показывает отправителей приглашений и не показывает друзей и приглашённых")
    void listsOnlyInviters(Page page,
                           @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user) {
        PeopleLogin.open(page, user)
                .openTab(PeopleTab.INCOME_INVITATIONS)
                .table()
                .checkThatPeople(List.of(user.incomeInvitations().getFirst().username()));
    }

    @Test
    @DisplayName("У приглашения кнопки Accept и Decline")
    void invitationOffersAcceptAndDecline(Page page, @User(incomeInvitations = 1) TestUser user) {
        PeopleLogin.open(page, user)
                .openTab(PeopleTab.INCOME_INVITATIONS)
                .table()
                .row(user.incomeInvitations().getFirst().username())
                .checkThatActions("Accept", "Decline");
    }

    @Test
    @DisplayName("Без входящих приглашений показывает пустое состояние")
    void showsEmptyStateWithoutInvitations(Page page, @User TestUser user) {
        PeopleLogin.open(page, user)
                .openTab(PeopleTab.INCOME_INVITATIONS)
                .table()
                .checkThatEmpty();
    }
}
