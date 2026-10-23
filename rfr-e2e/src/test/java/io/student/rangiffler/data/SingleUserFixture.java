package io.student.rangiffler.data;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.ApiProfile;

public final class SingleUserFixture implements UserFixture {
    private final TestUser user;
    private final AuthAccount account;
    private final ApiProfile profile;

    public SingleUserFixture(TestUser user) {
        this(user, new AuthAccount(user.username(), user.password()), new ApiProfile(user.username()));
    }

    public SingleUserFixture(TestUser user, AuthAccount account, ApiProfile profile) {
        this.user = user;
        this.account = account;
        this.profile = profile;
    }

    @Override
    public TestUser user() {
        return user;
    }

    @Override
    public void create() {
        account.create();
        profile.create();
    }

    @Override
    public void close() {
        new TearDown()
                .with(profile::delete)
                .with(account::delete)
                .run();
    }
}
