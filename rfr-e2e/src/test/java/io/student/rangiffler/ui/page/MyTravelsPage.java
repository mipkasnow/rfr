package io.student.rangiffler.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.ui.component.PhotoCard;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class MyTravelsPage {
    public static final String URL = TestConfig.frontUrl() + "/my-travels";
    private final Page page;
    private final Locator onlyMyTravels;
    private final Locator withFriends;
    private final Locator countriesVisited;
    private final Locator photoCards;

    public MyTravelsPage(Page page) {
        this.page = page;
        this.onlyMyTravels = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Only my travels").setExact(true));
        this.withFriends = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("With friends").setExact(true));
        this.countriesVisited = page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("countries visited"));
        this.photoCards = page.getByRole(AriaRole.ARTICLE);
    }

    @Step("Включить Only my travels")
    public MyTravelsPage clickOnlyMyTravels() {
        onlyMyTravels.click();
        return this;
    }

    @Step("Включить With friends")
    public MyTravelsPage clickWithFriends() {
        withFriends.click();
        return this;
    }

    @Step("Выбрать страну {country} в списке посещённых")
    public MyTravelsPage selectVisitedCountry(String country) {
        countriesVisited.click();
        page.getByRole(AriaRole.TOOLTIP)
                .getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName(country).setExact(true))
                .click();
        return this;
    }

    @Step("Сбросить фильтр страны")
    public MyTravelsPage resetCountryFilter() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Reset country filter").setExact(true)).click();
        return this;
    }

    public PhotoCard photo(String description) {
        return new PhotoCard(photoCards.filter(new Locator.FilterOptions().setHasText(description)), description);
    }

    @Step("Проверить число карточек фото: {count}")
    public MyTravelsPage checkThatPhotoCards(int count) {
        assertThat(photoCards).hasCount(count);
        return this;
    }

    @Step("Проверить текст счётчика стран: {text}")
    public MyTravelsPage checkThatCountriesVisited(String text) {
        assertThat(countriesVisited).hasText(text);
        return this;
    }

    @Step("Проверить чип фильтра страны {country}")
    public MyTravelsPage checkThatCountryFilter(String country) {
        assertThat(page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Country filter: " + country).setExact(true))).isVisible();
        return this;
    }
}
