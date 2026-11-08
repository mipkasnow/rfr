package io.student.rangiffler.session.fixture;

public interface SessionFixture<T> extends AutoCloseable {
    T session();

    void create();

    @Override
    void close();
}
