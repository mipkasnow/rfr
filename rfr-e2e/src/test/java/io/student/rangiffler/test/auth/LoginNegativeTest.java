package io.student.rangiffler.test.auth;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.LandingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("Негативный вход")
class LoginNegativeTest {
    private static final String BAD_CREDENTIALS = "Bad credentials";
    private static final String WRONG_PASSWORD = "wrong-password";

    @Test
    @DisplayName("Неверный пароль показывает Bad credentials")
    void wrongPasswordShowsBadCredentials(Page page, @User TestUser user) {
        new LandingPage(page).open()
                .clickLogin()
                .fillLoginForm(user.username(), WRONG_PASSWORD)
                .clickSignInExpectingError()
                .checkThatLoginError(BAD_CREDENTIALS);
    }

    @Test
    @DisplayName("Несуществующий пользователь показывает Bad credentials")
    void unknownUserShowsBadCredentials(Page page) {
        TestUser user = TestUser.unique();
        new LandingPage(page).open()
                .clickLogin()
                .fillLoginForm(user.username(), user.password())
                .clickSignInExpectingError()
                .checkThatLoginError(BAD_CREDENTIALS);
    }
}
