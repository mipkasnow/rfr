package io.student.rangiffler.session;

import io.student.rangiffler.network.GqlStubs;
import io.student.rangiffler.ui.page.LandingPage;

import java.util.Optional;

public record GuestSession(LandingPage landingPage, Optional<GqlStubs> stubs) {
}
