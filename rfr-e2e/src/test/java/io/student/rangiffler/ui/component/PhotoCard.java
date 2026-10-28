package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PhotoCard {
    private final Locator root;
    private final String description;

    public PhotoCard(Locator root, String description) {
        this.root = root;
        this.description = description;
    }

    @Step("Нажать like у фото {this.description}")
    public PhotoCard clickLike() {
        root.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("like").setExact(true)).click();
        return this;
    }

    @Step("Открыть Photo actions у фото {this.description}")
    public PhotoCard clickPhotoActions() {
        root.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Photo actions").setExact(true)).click();
        return this;
    }

    @Step("Проверить, что у фото {this.description} есть Photo actions")
    public PhotoCard checkThatPhotoActionsVisible() {
        assertThat(root.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Photo actions").setExact(true))).isVisible();
        return this;
    }

    @Step("Проверить, что у фото {this.description} нет Photo actions")
    public PhotoCard checkThatPhotoActionsHidden() {
        assertThat(root.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Photo actions").setExact(true))).hasCount(0);
        return this;
    }
}
