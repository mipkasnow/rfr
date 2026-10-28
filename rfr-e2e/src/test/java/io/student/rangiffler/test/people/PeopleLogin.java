package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.PeoplePage;

final class PeopleLogin {
    private PeopleLogin() {
    }

    static PeoplePage open(Page page, TestUser user) {
        return new LandingPage(page).open()
                .clickLogin()
                .fillLoginForm(user.username(), user.password())
                .clickSignIn()
                .clickPeople();
    }
}
