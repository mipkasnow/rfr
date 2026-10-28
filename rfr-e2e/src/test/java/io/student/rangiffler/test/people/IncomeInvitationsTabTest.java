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
@DisplayName("Тесты входящих запросов дружбы")
public class IncomeInvitationsTabTest {

    @Test
    @DisplayName("Пользователь с 1 исходящей заявкой и 1 другом не имеет входящих заявок на вкладке Income invitations")
    void incomeInvitationsEmptyList(Page page, @User(outcomeInvitations = 1, friends = 1) TestUser user) {
        incomeInvitations(page, user).checkThatIncomeInvitationsListIsEmpty();
    }

    @Test
    @DisplayName("Пользователь с 1 другом и 1 входящей заявкой видит кнопки Accept и Decline на вкладке Income invitations")
    void incomeInvitationsList(Page page, @User(friends = 1, incomeInvitations = 1) TestUser user) {
        String inviter = user.incomeInvitations().getFirst().username();
        incomeInvitations(page, user)
                .checkThatPeople(List.of(inviter))
                .checkThatAcceptAndDeclineButtons(inviter);
    }

    private PeoplePage incomeInvitations(Page page, TestUser user) {
        return new LandingPage(page).open()
                .clickLogin()
                .fillLoginForm(user.username(), user.password())
                .clickSignIn()
                .clickPeople()
                .clickIncomeInvitations();
    }
}
