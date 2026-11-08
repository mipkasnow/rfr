package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PhotoCard {
    private final Locator like;
    private final Locator photoActions;
    private final String description;

    public PhotoCard(Locator root, String description) {
        this.description = description;
        this.like = root.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("like").setExact(true));
        this.photoActions = root.getByRole(AriaRole.BUTTON,
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
}
