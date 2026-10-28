package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.StaticUser;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.utils.StaticUserPool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("engine-probe")
@DisplayName("UsersQueueExtension")
class UsersQueueExtensionTest {
    private static final Set<String> POOL = Set.of("staticUser1", "staticUser2", "staticUser3");

    @Test
    @DisplayName("Четыре теста подряд на пуле из трёх зелёные")
    void fourTestsOnPoolOfThreeSucceed() {
        assertEquals(4L, run(FourInARow.class).testEvents().succeeded().count(),
                "Four tests in a row on a pool of three did not all succeed");
    }

    @Test
    @DisplayName("После упавшего теста пользователь вернулся в очередь")
    void userReturnsToQueueAfterFailure() {
        EngineExecutionResults results = run(Failing.class);
        String username = entries(results).get("username");
        assertEquals(1L, results.testEvents().failed().count(), "Failing probe did not fail");
        assertEquals(POOL, Set.copyOf(StaticUserPool.usernames()),
                "Queue did not contain the original three users after the failed test");
        assertTrue(StaticUserPool.usernames().contains(username),
                "User taken by the failed test did not return to the queue");
    }

    private EngineExecutionResults run(Class<?> target) {
        return EngineTestKit.engine("junit-jupiter")
                .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                .selectors(selectClass(target))
                .execute();
    }

    private Map<String, String> entries(EngineExecutionResults results) {
        Map<String, String> entries = new HashMap<>();
        results.allEvents().reportingEntryPublished().stream()
                .map(event -> event.getRequiredPayload(ReportEntry.class))
                .forEach(entry -> entries.putAll(entry.getKeyValuePairs()));
        return entries;
    }

    @ProbeTarget
    static final class FourInARow {
        @Test
        void first(@StaticUser TestUser user) {
            hold(user);
        }

        @Test
        void second(@StaticUser TestUser user) {
            hold(user);
        }

        @Test
        void third(@StaticUser TestUser user) {
            hold(user);
        }

        @Test
        void fourth(@StaticUser TestUser user) {
            hold(user);
        }

        private static void hold(TestUser user) {
            if (user.username() == null) {
                throw new IllegalStateException("Static user was not taken from the queue");
            }
        }
    }

    @ProbeTarget
    static final class Failing {
        @Test
        void fail(TestReporter reporter, @StaticUser TestUser user) {
            reporter.publishEntry("username", user.username());
            throw new IllegalStateException("Probe target fails on purpose");
        }
    }
}
