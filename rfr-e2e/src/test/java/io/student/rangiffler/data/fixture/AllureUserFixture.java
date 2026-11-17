package io.student.rangiffler.data.fixture;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureExternalKey;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.FixtureResult;
import io.qameta.allure.model.Status;
import io.qameta.allure.util.ResultsUtils;
import io.student.rangiffler.data.TestUser;

import java.util.function.BiConsumer;

public final class AllureUserFixture implements UserFixture {
    private final UserFixture origin;
    private final AllureExternalKey scope;
    private final AllureLifecycle lifecycle;

    public AllureUserFixture(UserFixture origin, AllureExternalKey scope) {
        this(origin, scope, Allure.getLifecycle());
    }

    public AllureUserFixture(UserFixture origin, AllureExternalKey scope, AllureLifecycle lifecycle) {
        this.origin = origin;
        this.scope = scope;
        this.lifecycle = lifecycle;
    }

    @Override
    public TestUser user() {
        return origin.user();
    }

    @Override
    public void create() {
        run((key, result) -> lifecycle.startBeforeFixture(scope, key, result),
                "Создать пользователя " + origin.user().username(), origin::create);
    }

    @Override
    public void close() {
        run((key, result) -> lifecycle.startAfterFixture(scope, key, result),
                "Удалить пользователя " + origin.user().username(), origin::close);
    }

    private void run(BiConsumer<AllureExternalKey, FixtureResult> start, String name, Runnable action) {
        AllureExternalKey key = AllureExternalKey.random(AllureUserFixture.class);
        start.accept(key, new FixtureResult().setName(name));
        try {
            action.run();
            lifecycle.updateFixture(key, fixture -> fixture.setStatus(Status.PASSED));
        } catch (RuntimeException failure) {
            lifecycle.updateFixture(key, fixture -> {
                ResultsUtils.getStatus(failure).ifPresent(fixture::setStatus);
                ResultsUtils.getStatusDetails(failure).ifPresent(fixture::setStatusDetails);
            });
            throw failure;
        } finally {
            lifecycle.stopFixture(key);
        }
    }
}
