package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.db.ApiProfile;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.FriendshipStatus;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("engine-probe")
@DisplayName("UserExtension")
class UserExtensionTest {
    @Test
    @DisplayName("Тест с @User проходит")
    void testWithUserSucceeds() {
        assertEquals(1L, run(Observed.class).testEvents().succeeded().count(), "Test with @User did not succeed");
    }

    @Test
    @DisplayName("Во время теста пользователь есть в auth")
    void accountExistsDuringTest() {
        assertEquals("true", runAndReturnEntries(Observed.class).get("account"), "Account did not exist during the test");
    }

    @Test
    @DisplayName("Во время теста у пользователя есть профиль в api")
    void profileExistsDuringTest() {
        assertEquals("true", runAndReturnEntries(Observed.class).get("profile"), "Profile did not exist during the test");
    }

    @Test
    @DisplayName("Во время теста друг связан со статусом ACCEPTED")
    void friendshipIsAcceptedDuringTest() {
        assertEquals("true", runAndReturnEntries(Observed.class).get("friendship"), "Accepted friendship did not exist during the test");
    }

    @Test
    @DisplayName("Во время теста входящая заявка связана со статусом PENDING")
    void invitationIsPendingDuringTest() {
        assertEquals("true", runAndReturnEntries(Observed.class).get("invitation"), "Pending invitation did not exist during the test");
    }

    @Test
    @DisplayName("В параметр приходит пользователь с запрошенными друзьями")
    void resolvedUserCarriesFriends() {
        assertEquals("1", runAndReturnEntries(Observed.class).get("friends"), "Resolved user did not carry the requested friends");
    }

    @Test
    @DisplayName("В параметр приходит пользователь с запрошенными входящими заявками")
    void resolvedUserCarriesIncomeInvitations() {
        assertEquals("1", runAndReturnEntries(Observed.class).get("incomeInvitations"),
                "Resolved user did not carry the requested income invitations");
    }

    @Test
    @DisplayName("После теста пользователя нет в auth")
    void accountIsRemovedAfterTest() {
        Map<String, String> entries = runAndReturnEntries(Observed.class);
        assertFalse(new AuthAccount(entries.get("username"), entries.get("password")).exists(),
                "Account was not removed after the test");
    }

    @Test
    @DisplayName("После теста профиля нет в api")
    void profileIsRemovedAfterTest() {
        assertFalse(new ApiProfile(runAndReturnEntries(Observed.class).get("username")).exists(),
                "Profile was not removed after the test");
    }

    @Test
    @DisplayName("После теста профиля друга нет в api")
    void friendProfileIsRemovedAfterTest() {
        assertFalse(new ApiProfile(runAndReturnEntries(Observed.class).get("friend")).exists(),
                "Friend profile was not removed after the test");
    }

    @Test
    @DisplayName("После теста профиля пригласившего нет в api")
    void inviterProfileIsRemovedAfterTest() {
        assertFalse(new ApiProfile(runAndReturnEntries(Observed.class).get("inviter")).exists(),
                "Inviter profile was not removed after the test");
    }

    @Test
    @DisplayName("После упавшего теста профиля нет в api")
    void profileIsRemovedAfterFailedTest() {
        assertFalse(new ApiProfile(runAndReturnEntries(Failing.class).get("username")).exists(),
                "Profile was not removed after the failed test");
    }

    @Test
    @DisplayName("Два параметра @User получают разных пользователей")
    void twoParametersGetDifferentUsers() {
        Map<String, String> entries = runAndReturnEntries(TwoUsers.class);
        assertNotEquals(entries.get("first"), entries.get("second"), "Two @User parameters got the same user");
    }

    @Test
    @DisplayName("Отрицательное число друзей валит тест до создания пользователя")
    void negativeFriendsFailTest() {
        assertInstanceOf(IllegalArgumentException.class, failure(NegativeFriends.class),
                "Negative friends count did not fail the test with IllegalArgumentException");
    }

    @Test
    @DisplayName("TestUser без @User не резолвится")
    void userWithoutAnnotationIsNotResolved() {
        assertInstanceOf(ParameterResolutionException.class, failure(Unannotated.class),
                "TestUser without @User was resolved");
    }

    @Test
    @DisplayName("@User в параметре @BeforeEach не резолвится")
    void userInBeforeEachIsNotResolved() {
        assertInstanceOf(ParameterResolutionException.class, failure(BeforeEachUser.class),
                "@User in @BeforeEach parameter was resolved");
    }

    private EngineExecutionResults run(Class<?> target) {
        return EngineTestKit.engine("junit-jupiter")
                .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                .selectors(selectClass(target))
                .execute();
    }

    private Map<String, String> runAndReturnEntries(Class<?> target) {
        Map<String, String> entries = new HashMap<>();
        run(target).allEvents().reportingEntryPublished().stream()
                .map(event -> event.getRequiredPayload(ReportEntry.class))
                .forEach(entry -> entries.putAll(entry.getKeyValuePairs()));
        return entries;
    }

    private Throwable failure(Class<?> target) {
        return run(target).testEvents().failed().stream()
                .map(event -> event.getRequiredPayload(TestExecutionResult.class))
                .flatMap(result -> result.getThrowable().stream())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Probe target " + target.getSimpleName() + " did not fail"));
    }

    @ProbeTarget
    static final class Observed {
        @Test
        void observe(TestReporter reporter, @User(friends = 1, incomeInvitations = 1) TestUser user) {
            TestUser friend = user.friends().getFirst();
            TestUser inviter = user.incomeInvitations().getFirst();
            reporter.publishEntry(Map.of(
                    "username", user.username(),
                    "password", user.password(),
                    "friend", friend.username(),
                    "inviter", inviter.username(),
                    "friends", String.valueOf(user.friends().size()),
                    "incomeInvitations", String.valueOf(user.incomeInvitations().size())));
            reporter.publishEntry(Map.of(
                    "account", String.valueOf(new AuthAccount(user.username(), user.password()).exists()),
                    "profile", String.valueOf(new ApiProfile(user.username()).exists()),
                    "friendship", String.valueOf(
                            new Friendship(user.username(), friend.username(), FriendshipStatus.ACCEPTED).exists()),
                    "invitation", String.valueOf(
                            new Friendship(inviter.username(), user.username(), FriendshipStatus.PENDING).exists())));
        }
    }

    @ProbeTarget
    static final class Failing {
        @Test
        void fail(TestReporter reporter, @User TestUser user) {
            reporter.publishEntry("username", user.username());
            throw new IllegalStateException("Probe target fails on purpose");
        }
    }

    @ProbeTarget
    static final class TwoUsers {
        @Test
        void observe(TestReporter reporter, @User TestUser first, @User TestUser second) {
            reporter.publishEntry(Map.of("first", first.username(), "second", second.username()));
        }
    }

    @ProbeTarget
    static final class NegativeFriends {
        @Test
        void observe(@User(friends = -1) TestUser user) {
        }
    }

    @ProbeTarget
    static final class Unannotated {
        @Test
        void observe(TestUser user) {
        }
    }

    @ProbeTarget
    static final class BeforeEachUser {
        @BeforeEach
        void prepare(@User TestUser user) {
        }

        @Test
        void observe() {
        }
    }
}
