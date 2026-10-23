package io.student.rangiffler.data;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;

import java.util.ArrayList;
import java.util.List;

public final class UserWithFriendsFixture implements UserFixture {
    private final UserFixture origin;
    private final int count;
    private final List<UserFixture> friends = new ArrayList<>();

    public UserWithFriendsFixture(UserFixture origin, int count) {
        this.origin = origin;
        this.count = count;
    }

    @Override
    public TestUser user() {
        return origin.user().withFriends(friends.stream().map(UserFixture::user).toList());
    }

    @Override
    public void create() {
        origin.create();
        for (int index = 0; index < count; index++) {
            UserFixture friend = new SingleUserFixture(TestUser.unique());
            friends.add(friend);
            friend.create();
            new Friendship(origin.user().username(), friend.user().username(), FriendshipStatus.ACCEPTED).create();
        }
    }

    @Override
    public void close() {
        new TearDown()
                .withEach(friends, UserFixture::close)
                .with(origin::close)
                .run();
    }
}
