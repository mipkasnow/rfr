package io.student.rangiffler.session.fixture;

import com.microsoft.playwright.BrowserContext;
import io.student.rangiffler.data.TearDown;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.network.GqlStubs;
import io.student.rangiffler.session.UserSession;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.MainPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UserSessionFixture implements SessionFixture<UserSession> {
    private final BrowserContext context;
    private final TestUser user;
    private final Optional<GqlStubs> stubs;
    private final List<MainPage> signedIn = new ArrayList<>(1);

    public UserSessionFixture(BrowserContext context, TestUser user, Optional<GqlStubs> stubs) {
        this.context = context;
        this.user = user;
        this.stubs = stubs;
    }

    @Override
    public UserSession session() {
        if (signedIn.isEmpty()) {
            throw new IllegalStateException("User session of " + user.username() + " was not created");
        }
        return new UserSession(user, signedIn.getFirst(), stubs);
    }

    @Override
    public void create() {
        signedIn.add(new LandingPage(context.newPage()).open()
                .clickLogin()
                .fillLoginForm(user.username(), user.password())
                .clickSignIn());
    }

    @Override
    public void close() {
        new TearDown()
                .with(() -> stubs.ifPresent(GqlStubs::close))
                .with(context::close)
                .run();
    }
}
