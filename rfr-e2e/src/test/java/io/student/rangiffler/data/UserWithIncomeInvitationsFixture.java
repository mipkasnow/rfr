package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;

import java.util.ArrayList;
import java.util.List;

public final class UserWithIncomeInvitationsFixture implements UserFixture {
    private final UserFixture origin;
    private final int count;
    private final List<UserFixture> inviters = new ArrayList<>();

    public UserWithIncomeInvitationsFixture(UserFixture origin, int count) {
        this.origin = origin;
        this.count = count;
    }

    @Override
    public TestUser user() {
        return origin.user().withIncomeInvitations(inviters.stream().map(UserFixture::user).toList());
    }

    @Override
    public void create() {
        origin.create();
        for (int index = 0; index < count; index++) {
            UserFixture inviter = new SingleUserFixture(TestUser.unique());
            inviters.add(inviter);
            inviter.create();
            new Friendship(inviter.user().username(), origin.user().username(), FriendshipStatus.PENDING).create();
        }
    }

    @Override
    public void close() {
        new TearDown()
                .withEach(inviters, UserFixture::close)
                .with(origin::close)
                .run();
    }
}
