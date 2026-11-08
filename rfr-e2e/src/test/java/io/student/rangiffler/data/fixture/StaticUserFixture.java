package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.utils.StaticUserPool;

public final class StaticUserFixture implements UserFixture {

    private TestUser user;

    @Override
    public TestUser user() {
        return user;
    }

    @Override
    public void create() {
        this.user = StaticUserPool.getFreeStaticUser();
    }

    @Override
    public void close() {
        StaticUserPool.returnStaticUserToQueue(this.user);
    }
}
