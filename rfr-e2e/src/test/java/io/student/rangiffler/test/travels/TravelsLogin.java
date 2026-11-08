package io.student.rangiffler.test.travels;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.MyTravelsPage;

final class TravelsLogin {
    private TravelsLogin() {
    }

    static MyTravelsPage open(Page page, TestUser user) {
        new LandingPage(page).open()
                .clickLogin()
                .fillLoginForm(user.username(), user.password())
                .clickSignIn();
        return new MyTravelsPage(page);
    }
}
