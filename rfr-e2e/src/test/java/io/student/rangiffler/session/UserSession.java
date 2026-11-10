package io.student.rangiffler.session;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.network.GqlStubs;
import io.student.rangiffler.ui.page.MainPage;

import java.util.Optional;

public record UserSession(TestUser user, MainPage mainPage, Optional<GqlStubs> stubs) {
}
