package io.student.rangiffler.data.fixture;

import io.student.rangiffler.data.TestUser;

public interface UserFixture extends AutoCloseable {
    TestUser user();

    void create();

    @Override
    void close();
}
