package io.student.rangiffler.test.travels;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("Add photo: ARIA диалога")
class AddPhotoDialogAriaTest {
    @Test
    @DisplayName("Диалог добавления фото совпадает с ARIA-снимком")
    void dialogMatchesAriaSnapshot(Page page, @User TestUser user) {
        TravelsLogin.open(page, user)
                .clickAddPhoto()
                .checkThatAriaSnapshot("Russian Federation");
    }
}
