package io.student.rangiffler.session;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.ui.page.MainPage;

public record UserSession(TestUser user, MainPage mainPage) {
}
