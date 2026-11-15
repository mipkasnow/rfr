package io.student.rangiffler.test.travels;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.MockPhoto;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.Theme;
import io.student.rangiffler.jupiter.annotation.ThemeMode;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("My Travels: скриншоты mock-ленты")
class MyTravelsVisualTest {
    abstract static class FeedScreenshots {
        abstract String cardBaseline();

        abstract String feedBaseline();

        @Test
        @DisplayName("Карточка фото совпадает с эталоном")
        void cardMatchesBaseline(Page page, @User TestUser user) {
            TravelsLogin.open(page, user)
                    .photo(MockPhoto.PARIS.description())
                    .checkThatScreenshot(cardBaseline());
        }

        @Test
        @DisplayName("Лента своих фото совпадает с эталоном")
        void feedMatchesBaseline(Page page, @User TestUser user) {
            TravelsLogin.open(page, user)
                    .checkThatPhotoCards(4)
                    .checkThatCardsScreenshot(feedBaseline());
        }
    }

    @Nested
    @DisplayName("светлая тема")
    class Light extends FeedScreenshots {
        @Override
        String cardBaseline() {
            return "travels-card-paris-light";
        }

        @Override
        String feedBaseline() {
            return "travels-feed-only-mine-light";
        }
    }

    @Nested
    @Theme(ThemeMode.DARK)
    @DisplayName("тёмная тема")
    class Dark extends FeedScreenshots {
        @Override
        String cardBaseline() {
            return "travels-card-paris-dark";
        }

        @Override
        String feedBaseline() {
            return "travels-feed-only-mine-dark";
        }
    }
}
