package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.ScreenshotAnimations;
import com.microsoft.playwright.options.ScreenshotCaret;
import io.qameta.allure.Step;
import io.student.rangiffler.visual.BaselineStore;
import io.student.rangiffler.visual.ScreenshotAssertions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public final class PhotoCard {
    private final Locator card;
    private final Locator like;
    private final Locator photoActions;
    private final String description;

    public PhotoCard(Locator card, String description) {
        this.card = card;
        this.description = description;
        this.like = card.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("like").setExact(true));
        this.photoActions = card.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Photo actions").setExact(true));
    }

    @Step("Нажать like у фото {this.description}")
    public PhotoCard clickLike() {
        like.click();
        return this;
    }

    @Step("Открыть Photo actions у фото {this.description}")
    public PhotoCard clickPhotoActions() {
        photoActions.click();
        return this;
    }

    @Step("Удалить фото {this.description} через меню Photo actions")
    public PhotoCard clickDelete() {
        photoActions.click();
        card.page().getByRole(AriaRole.MENUITEM,
                new Page.GetByRoleOptions().setName("Delete").setExact(true)).click();
        return this;
    }

    @Step("Проверить, что у фото {this.description} есть Photo actions")
    public PhotoCard checkThatPhotoActionsVisible() {
        assertThat(photoActions).isVisible();
        return this;
    }

    @Step("Проверить, что у фото {this.description} нет Photo actions")
    public PhotoCard checkThatPhotoActionsHidden() {
        assertThat(photoActions).hasCount(0);
        return this;
    }

    @Step("Проверить лайки фото {this.description}: {likes}")
    public PhotoCard checkThatLikes(String likes) {
        String label = "1".equals(likes) ? "1 like" : likes + " likes";
        assertAll("Фото " + description + " не показывает кнопку лайка с числом лайков " + likes,
                () -> assertThat(like).isVisible(),
                () -> assertThat(card.getByText(label, new Locator.GetByTextOptions().setExact(true))).isVisible());
        return this;
    }

    @Step("Проверить ARIA-снимок фото {this.description}: страна {country}, меню владельца {own}, лайк нажат {liked}, лайков {likes}")
    public PhotoCard checkThatAriaSnapshot(String country, boolean own, boolean liked, int likes) {
        assertThat(card).matchesAriaSnapshot("""
                - article "%1$s %2$s":
                  - /children: deep-equal
                  - img "%2$s"
                  - text: %1$s
                %3$s  - paragraph: %2$s
                  - button "like" [pressed=%4$b]
                  - paragraph: %5$s
                """.formatted(country, description, own ? "  - button \"Photo actions\"\n" : "", liked, likesText(likes)));
        return this;
    }

    @Step("Проверить скриншот фото {this.description} по эталону {name}")
    public PhotoCard checkThatScreenshot(String name) {
        assertThat(card.getByRole(AriaRole.IMG, new Locator.GetByRoleOptions().setName(description).setExact(true)))
                .hasJSProperty("complete", true);
        new ScreenshotAssertions(BaselineStore.current()).assertMatches(name, card.screenshot(
                new Locator.ScreenshotOptions().setAnimations(ScreenshotAnimations.DISABLED).setCaret(ScreenshotCaret.HIDE)));
        return this;
    }

    private String likesText(int likes) {
        return likes + (likes == 1 ? " like" : " likes");
    }
}
