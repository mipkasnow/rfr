package io.student.rangiffler.session.fixture;

import com.microsoft.playwright.BrowserContext;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.session.UserSession;
import io.student.rangiffler.ui.page.LandingPage;
import io.student.rangiffler.ui.page.MainPage;

import java.util.ArrayList;
import java.util.List;

public final class UserSessionFixture implements SessionFixture<UserSession> {
    private final BrowserContext context;
    private final TestUser user;
    private final List<MainPage> signedIn = new ArrayList<>(1);

    public UserSessionFixture(BrowserContext context, TestUser user) {
        this.context = context;
        this.user = user;
    }

    @Override
    public UserSession session() {
        if (signedIn.isEmpty()) {
            throw new IllegalStateException("User session of " + user.username() + " was not created");
        }
        return new UserSession(user, signedIn.getFirst());
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
        context.close();
    }
}
