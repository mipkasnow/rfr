package io.student.rangiffler.test.auth;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.Session;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.session.UserSession;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.PeopleTab;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("Изоляция браузерных сессий")
class SessionIsolationTest {
    @Test
    @DisplayName("Выход в одном контексте не разлогинивает другой")
    void logoutInOneContextKeepsOtherSignedIn(Page page, @Session @User(friends = 1) UserSession bob) {
        TestUser friend = bob.user().friends().getFirst();
        new LandingPage(page).open()
                .clickLogin()
                .fillLoginForm(bob.user().username(), bob.user().password())
                .clickSignIn()
                .clickLogout()
                .checkThatGuestVisible();
        bob.mainPage()
                .clickPeople()
                .openTab(PeopleTab.FRIENDS)
                .table()
                .checkThatEmpty();
    }
}
