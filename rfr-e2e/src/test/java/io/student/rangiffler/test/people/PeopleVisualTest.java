package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.Theme;
import io.student.rangiffler.jupiter.annotation.ThemeMode;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("People: скриншоты пустого состояния")
class PeopleVisualTest {
    abstract static class EmptyPeopleScreenshots {
        abstract String baseline();

        @Test
        @DisplayName("Пустой список друзей совпадает с эталоном")
        void emptyFriendsMatchesBaseline(Page page, @User TestUser user) {
            PeopleLogin.open(page, user)
                    .table()
                    .checkThatEmptyScreenshot(baseline());
        }
    }

    @Nested
    @Theme(ThemeMode.DARK)
    @DisplayName("светлая тема")
    class Light extends EmptyPeopleScreenshots {
        @Override
        String baseline() {
            return "people-empty-light";
        }
    }

    @Nested
    @Theme(ThemeMode.DARK)
    @DisplayName("тёмная тема")
    class Dark extends EmptyPeopleScreenshots {
        @Override
        String baseline() {
            return "people-empty-dark";
        }
    }
}
