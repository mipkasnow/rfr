package io.student.rangiffler.session.fixture;

import com.microsoft.playwright.BrowserContext;
import io.student.rangiffler.data.TearDown;
import io.student.rangiffler.network.GqlStubs;
import io.student.rangiffler.session.GuestSession;
import io.student.rangiffler.ui.page.LandingPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GuestSessionFixture implements SessionFixture<GuestSession> {
    private final BrowserContext context;
    private final Optional<GqlStubs> stubs;
    private final List<LandingPage> opened = new ArrayList<>(1);

    public GuestSessionFixture(BrowserContext context, Optional<GqlStubs> stubs) {
        this.context = context;
        this.stubs = stubs;
    }

    @Override
    public GuestSession session() {
        if (opened.isEmpty()) {
            throw new IllegalStateException("Guest session was not created");
        }
        return new GuestSession(opened.getFirst(), stubs);
    }

    @Override
    public void create() {
        opened.add(new LandingPage(context.newPage()).open());
    }

    @Override
    public void close() {
        new TearDown()
                .with(() -> stubs.ifPresent(GqlStubs::close))
                .with(context::close)
                .run();
    }
}
