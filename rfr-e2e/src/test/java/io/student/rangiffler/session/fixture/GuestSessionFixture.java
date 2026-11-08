package io.student.rangiffler.session.fixture;

import com.microsoft.playwright.BrowserContext;
import io.student.rangiffler.session.GuestSession;
import io.student.rangiffler.ui.page.LandingPage;

import java.util.ArrayList;
import java.util.List;

public final class GuestSessionFixture implements SessionFixture<GuestSession> {
    private final BrowserContext context;
    private final List<LandingPage> opened = new ArrayList<>(1);

    public GuestSessionFixture(BrowserContext context) {
        this.context = context;
    }

    @Override
    public GuestSession session() {
        if (opened.isEmpty()) {
            throw new IllegalStateException("Guest session was not created");
        }
        return new GuestSession(opened.getFirst());
    }

    @Override
    public void create() {
        opened.add(new LandingPage(context.newPage()).open());
    }

    @Override
    public void close() {
        context.close();
    }
}
