package io.student.rangiffler.data;

import java.util.ArrayList;
import java.util.List;

public final class UserWithRelationsFixture implements UserFixture {
    private final UserFixture origin;
    private final Relation relation;
    private final int count;
    private final List<UserFixture> related = new ArrayList<>();

    public UserWithRelationsFixture(UserFixture origin, Relation relation, int count) {
        this.origin = origin;
        this.relation = relation;
        this.count = count;
    }

    @Override
    public TestUser user() {
        return relation.attach(origin.user(), related.stream().map(UserFixture::user).toList());
    }

    @Override
    public void create() {
        origin.create();
        for (int index = 0; index < count; index++) {
            UserFixture user = new SingleUserFixture(TestUser.unique());
            related.add(user);
            user.create();
            relation.link(origin.user().username(), user.user().username());
        }
    }

    @Override
    public void close() {
        new TearDown()
                .withEach(related, UserFixture::close)
                .with(origin::close)
                .run();
    }
}
