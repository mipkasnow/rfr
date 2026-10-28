package io.student.rangiffler.utils;

import io.student.rangiffler.data.TestUser;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

@UtilityClass
public final class StaticUserPool {
    private static final long WAIT_TIMEOUT_SECONDS;
    private static final long WAIT_SLEEP_MS;
    private static final ConcurrentLinkedDeque<TestUser> STATIC_USERS_QUEUE;

    static {
        WAIT_TIMEOUT_SECONDS = 60L;
        WAIT_SLEEP_MS = 100L;
        STATIC_USERS_QUEUE = new ConcurrentLinkedDeque<>(
                List.of(
                        new TestUser("staticUser1", "123"),
                        new TestUser("staticUser2", "123"),
                        new TestUser("staticUser3", "123")
                )
        );
    }

    public static TestUser getFreeStaticUser() {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(WAIT_TIMEOUT_SECONDS);
        TestUser user = STATIC_USERS_QUEUE.pollFirst();
        while (user == null && System.nanoTime() < deadline) {
            sleep();
            user = STATIC_USERS_QUEUE.pollFirst();
        }
        if (user == null) {
            throw new IllegalStateException("Can't obtain free user within %s seconds.".formatted(WAIT_TIMEOUT_SECONDS));
        }
        return user;
    }

    public static List<String> usernames() {
        return STATIC_USERS_QUEUE.stream().map(TestUser::username).toList();
    }

    public static void returnStaticUserToQueue(TestUser user) {
        if (user != null) {
            STATIC_USERS_QUEUE.offerLast(user);
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(WAIT_SLEEP_MS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
