package io.student.rangiffler.test.travels;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.MockPhoto;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("My Travels: скриншоты mock-ленты")
class MyTravelsVisualTest {
    @Test
    @DisplayName("Карточка фото совпадает с эталоном")
    void cardMatchesBaseline(Page page, @User TestUser user) {
        TravelsLogin.open(page, user)
                .photo(MockPhoto.PARIS.description())
                .checkThatScreenshot("travels-card-paris");
    }

    @Test
    @DisplayName("Лента своих фото совпадает с эталоном")
    void feedMatchesBaseline(Page page, @User TestUser user) {
        TravelsLogin.open(page, user)
                .checkThatPhotoCards(4)
                .checkThatCardsScreenshot("travels-feed-only-mine");
    }
}
