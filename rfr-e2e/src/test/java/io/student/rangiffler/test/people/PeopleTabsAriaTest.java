package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UiTest
@DisplayName("People: ARIA вкладок")
class PeopleTabsAriaTest {
    @Test
    @DisplayName("Вкладки People на пустых Friends совпадают с ARIA-снимком")
    void emptyFriendsTabsMatchAriaSnapshot(Page page, @User TestUser user) {
        PeopleLogin.open(page, user)
                .checkThatAriaSnapshot();
    }
}
