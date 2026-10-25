package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;

import java.util.ArrayList;
import java.util.List;

public final class UserWithOutcomeInvitationsFixture implements UserFixture {
    private final UserFixture origin;
    private final int count;
    private final List<UserFixture> addressees = new ArrayList<>();

    public UserWithOutcomeInvitationsFixture(UserFixture origin, int count) {
        this.origin = origin;
        this.count = count;
    }

    @Override
    public TestUser user() {
        return origin.user().withOutcomeInvitations(addressees.stream().map(UserFixture::user).toList());
    }

    @Override
    public void create() {
        origin.create();
        for (int index = 0; index < count; index++) {
            UserFixture addressee = new SingleUserFixture(TestUser.unique());
            addressees.add(addressee);
            addressee.create();
            new Friendship(origin.user().username(), addressee.user().username() , FriendshipStatus.PENDING).create();
        }
    }

    @Override
    public void close() {
        new TearDown()
                .withEach(addressees, UserFixture::close)
                .with(origin::close)
                .run();
    }
}
