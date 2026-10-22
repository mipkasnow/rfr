package io.student.rangiffler.test.auth;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.LandingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UsePlaywright(LoginNegativeTest.EnglishLocaleOptions.class)
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

    public static final class EnglishLocaleOptions implements OptionsFactory {
        @Override
        public Options getOptions() {
            return new Options()
                    .setContextOptions(new Browser.NewContextOptions().setLocale("en-US"));
        }
    }
}
