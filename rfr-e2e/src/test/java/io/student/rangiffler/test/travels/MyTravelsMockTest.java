package io.student.rangiffler.test.travels;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.ui.page.MyTravelsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("My Travels на mock-ленте")
class MyTravelsMockTest {
    private static final int ONLY_MINE_PHOTOS = 4;
    private static final int WITH_FRIENDS_PHOTOS = 7;
    private static final String ONLY_MINE_COUNTRIES = "4 countries visited";
    private static final String WITH_FRIENDS_COUNTRIES = "7 countries visited";
    private static final String FILTER_COUNTRY = "Netherlands";
    private static final String OWN_PHOTO = "Амстердам!";
    private static final String FRIEND_PHOTO = "Старая Прага завораживает!";

    @Test
    @DisplayName("Переключатель Only my travels / With friends меняет карточки и счётчик стран на mock-ленте")
    void toggleChangesCardsAndCountriesCounter(Page page, @User TestUser user) {
        TravelsLogin.open(page, user)
                .checkThatPhotoCards(ONLY_MINE_PHOTOS)
                .checkThatCountriesVisited(ONLY_MINE_COUNTRIES)
                .clickWithFriends()
                .checkThatPhotoCards(WITH_FRIENDS_PHOTOS)
                .checkThatCountriesVisited(WITH_FRIENDS_COUNTRIES)
                .clickOnlyMyTravels()
                .checkThatPhotoCards(ONLY_MINE_PHOTOS)
                .checkThatCountriesVisited(ONLY_MINE_COUNTRIES);
    }

    @Test
    @DisplayName("Фильтр по стране на mock-ленте показывает чип и одну карточку, сброс возвращает ленту")
    void countryFilterShowsChipAndSingleCard(Page page, @User TestUser user) {
        MyTravelsPage travels = TravelsLogin.open(page, user)
                .checkThatPhotoCards(ONLY_MINE_PHOTOS)
                .selectVisitedCountry(FILTER_COUNTRY)
                .checkThatCountryFilter(FILTER_COUNTRY)
                .checkThatPhotoCards(1);
        travels.photo(OWN_PHOTO).checkThatPhotoActionsVisible();
        travels.resetCountryFilter()
                .checkThatPhotoCards(ONLY_MINE_PHOTOS);
    }

    @Test
    @DisplayName("Photo actions на mock-ленте есть только у своих фото")
    void photoActionsOnlyOnOwnPhotos(Page page, @User TestUser user) {
        MyTravelsPage travels = TravelsLogin.open(page, user)
                .clickWithFriends()
                .checkThatPhotoCards(WITH_FRIENDS_PHOTOS);
        travels.photo(OWN_PHOTO).checkThatPhotoActionsVisible();
        travels.photo(FRIEND_PHOTO).checkThatPhotoActionsHidden();
    }
}
