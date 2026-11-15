package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

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
}
