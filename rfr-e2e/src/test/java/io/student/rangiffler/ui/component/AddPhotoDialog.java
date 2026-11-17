package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class AddPhotoDialog {
    private final Locator dialog;

    public AddPhotoDialog(Page page) {
        this.dialog = page.getByRole(AriaRole.DIALOG,
                new Page.GetByRoleOptions().setName("Add photo").setExact(true));
    }

    @Step("Проверить ARIA-снимок диалога Add photo: страна {country}")
    public AddPhotoDialog checkThatAriaSnapshot(String country) {
        assertThat(dialog).matchesAriaSnapshot("""
                - dialog "Add photo":
                  - /children: deep-equal
                  - heading "Add photo" [level=2]
                  - button "Upload new image"
                  - text: Country
                  - combobox "Country":
                    - img "%1$s"
                    - text: %1$s
                  - text: Description
                  - textbox "Description":
                    - /placeholder: About this photo
                  - button "Save"
                  - button "Close"
                """.formatted(country));
        return this;
    }
}
