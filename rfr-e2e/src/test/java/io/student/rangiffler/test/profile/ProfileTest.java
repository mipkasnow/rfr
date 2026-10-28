package io.student.rangiffler.test.profile;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.ProfilePage;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("Профиль")
class ProfileTest {
    private static final String FIRST_NAME = "Ada";
    private static final String SURNAME = "Lovelace";
    private static final String COUNTRY = "France";
    private static final String OTHER_FIRST_NAME = "Grace";
    private static final String OTHER_SURNAME = "Hopper";
    private static final String OTHER_COUNTRY = "Germany";
    private static final String AVATAR_SRC_PREFIX = "data:image/";

    @Test
    @DisplayName("First Name сохраняется и читается после перезагрузки")
    void savesFirstName(Page page, @User TestUser user) {
        openProfile(page, user)
                .fillFirstName(FIRST_NAME)
                .clickSave()
                .reload()
                .checkThatFirstName(FIRST_NAME);
    }

    @Test
    @DisplayName("Surname сохраняется и читается после перезагрузки")
    void savesSurname(Page page, @User TestUser user) {
        openProfile(page, user)
                .fillSurname(SURNAME)
                .clickSave()
                .reload()
                .checkThatSurname(SURNAME);
    }

    @Test
    @DisplayName("Страна сохраняется и читается после перезагрузки")
    void savesLocation(Page page, @User TestUser user) {
        openProfile(page, user)
                .selectLocation(COUNTRY)
                .clickSave()
                .reload()
                .checkThatLocation(COUNTRY);
    }

    @Test
    @DisplayName("Аватар сохраняется и читается после перезагрузки")
    void savesAvatar(Page page, @User TestUser user) {
        openProfile(page, user)
                .uploadAvatar(avatar())
                .clickSave()
                .reload()
                .checkThatAvatarSrcStartsWith(AVATAR_SRC_PREFIX);
    }

    @Test
    @DisplayName("Reset возвращает сохранённые значения")
    void resetRestoresSavedValues(Page page, @User TestUser user) {
        openProfile(page, user)
                .fillFirstName(FIRST_NAME)
                .fillSurname(SURNAME)
                .selectLocation(COUNTRY)
                .clickSave()
                .reload()
                .fillFirstName(OTHER_FIRST_NAME)
                .fillSurname(OTHER_SURNAME)
                .selectLocation(OTHER_COUNTRY)
                .clickReset()
                .checkThatFirstName(FIRST_NAME)
                .checkThatSurname(SURNAME)
                .checkThatLocation(COUNTRY);
    }

    private static ProfilePage openProfile(Page page, TestUser user) {
        return new LandingPage(page).open()
                .clickLogin()
                .fillLoginForm(user.username(), user.password())
                .clickSignIn()
                .clickProfile();
    }

    private static Path avatar() {
        try {
            return Path.of(Objects.requireNonNull(ProfileTest.class.getResource("/profile/avatar.png")).toURI());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Avatar resource is missing", exception);
        }
    }
}
