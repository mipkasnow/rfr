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
    @DisplayName("Во время теста исходящая заявка связана со статусом PENDING")
    void friendsShipRequestIsPendingDuringTest() {
        assertEquals("true", runAndReturnEntries(Observed.class).get("request"), "Pending invitation did not exist during the test");
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
    @DisplayName("В параметр приходит пользователь с которому отправлена заявка на дружбу")
    void resolvedUserCarriesOutcomeInvitations() {
        assertEquals("1", runAndReturnEntries(Observed.class).get("outcomeInvitations"),
                "Resolved user did not carry the requested outcome invitations");
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
    @DisplayName("После теста друга нет в auth")
    void accountFriendIsRemovedAfterTest() {
        Map<String, String> entries = runAndReturnEntries(Observed.class);
        assertFalse(new AuthAccount(entries.get("friend"), entries.get("friendPassword")).exists(),
                "Account was not removed after the test");
    }

    @Test
    @DisplayName("После теста профиля пригласившего нет в api")
    void inviterProfileIsRemovedAfterTest() {
        assertFalse(new ApiProfile(runAndReturnEntries(Observed.class).get("inviter")).exists(),
                "Inviter profile was not removed after the test");
    }

    @Test
    @DisplayName("После теста пригласившего нет в auth")
    void accountInviterIsRemovedAfterTest() {
        Map<String, String> entries = runAndReturnEntries(Observed.class);
        assertFalse(new AuthAccount(entries.get("inviter"), entries.get("inviterPassword")).exists(),
                "Account was not removed after the test");
    }

    @Test
    @DisplayName("После теста профиля желаемого друга нет в api")
    void addresseeProfileIsRemovedAfterTest() {
        assertFalse(new ApiProfile(runAndReturnEntries(Observed.class).get("addressee")).exists(),
                "Inviter profile was not removed after the test");
    }

    @Test
    @DisplayName("После теста исходящей заявки нет")
    void outcomeInvitationIsRemovedAfterTest() {
        Map<String, String> entries = runAndReturnEntries(Observed.class);
        assertFalse(new Friendship(entries.get("username"), entries.get("addressee"), FriendshipStatus.PENDING).exists(),
                "Outcome invitation was not removed after the test");
    }

    @Test
    @DisplayName("После теста желаемого друга нет в auth")
    void accountAddresseeIsRemovedAfterTest() {
        Map<String, String> entries = runAndReturnEntries(Observed.class);
        assertFalse(new AuthAccount(entries.get("addressee"), entries.get("addresseePassword")).exists(),
                "Account was not removed after the test");
    }

    @Test
    @DisplayName("После упавшего теста профиля нет в api")
    void profileIsRemovedAfterFailedTest() {
        assertFalse(new ApiProfile(runAndReturnEntries(Failing.class).get("username")).exists(),
                "Profile was not removed after the failed test");
    }

    @Test
    @DisplayName("После упавшего теста профиля нет в auth")
    void authIsRemovedAfterFailedTest() {
        Map<String, String> entries = runAndReturnEntries(Failing.class);
        assertFalse(new AuthAccount(entries.get("username"), entries.get("password")).exists(),
                "Account was not removed after the failed test");
    }

    @Test
    @DisplayName("После упавшего теста профиля друга нет в api")
    void friendProfileIsRemovedAfterFailedTest() {
        assertFalse(new ApiProfile(runAndReturnEntries(Failing.class).get("friend")).exists(),
                "Friend profile was not removed after the test");
    }

    @Test
    @DisplayName("После упавшего теста друга нет в auth")
    void accountFriendIsRemovedAfterFailedTest() {
        Map<String, String> entries = runAndReturnEntries(Failing.class);
        assertFalse(new AuthAccount(entries.get("friend"), entries.get("friendPassword")).exists(),
                "Account was not removed after the test");
    }

    @Test
    @DisplayName("После упавшего теста профиля пригласившего нет в api")
    void inviterProfileIsRemovedAfterFailedTest() {
        assertFalse(new ApiProfile(runAndReturnEntries(Failing.class).get("inviter")).exists(),
                "Inviter profile was not removed after the test");
    }

    @Test
    @DisplayName("После упавшего теста пригласившего нет в auth")
    void accountInviterIsRemovedAfterFailedTest() {
        Map<String, String> entries = runAndReturnEntries(Failing.class);
        assertFalse(new AuthAccount(entries.get("inviter"), entries.get("inviterPassword")).exists(),
                "Account was not removed after the test");
    }

    @Test
    @DisplayName("После упавшего теста профиля желаемого друга нет в api")
    void addresseeProfileIsRemovedAfterFailedTest() {
        assertFalse(new ApiProfile(runAndReturnEntries(Failing.class).get("addressee")).exists(),
                "Inviter profile was not removed after the test");
    }

    @Test
    @DisplayName("После упавшего теста желаемого друга нет в auth")
    void accountAddresseeIsRemovedAfterFailedTest() {
        Map<String, String> entries = runAndReturnEntries(Failing.class);
        assertFalse(new AuthAccount(entries.get("addressee"), entries.get("addresseePassword")).exists(),
                "Account was not removed after the test");
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
    @DisplayName("Отрицательное число входящих заявок валит тест до создания пользователя")
    void negativeIncomeInvitationsFailTest() {
        assertInstanceOf(IllegalArgumentException.class, failure(NegativeIncomeInvitations.class),
                "Negative invitations count did not fail the test with IllegalArgumentException");
    }

    @Test
    @DisplayName("Отрицательное число исходящих заявок валит тест до создания пользователя")
    void negativeOutcomeInvitationsFailTest() {
        IllegalArgumentException thrown = assertInstanceOf(IllegalArgumentException.class,
                failure(NegativeOutcomeInvitations.class),
                "Negative outcome invitations count did not fail the test with IllegalArgumentException");
        assertEquals("Outcome invitations count must be non-negative, got -1", thrown.getMessage());
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
        void observe(TestReporter reporter, @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user) {
            reporter.publishEntry(buildEntries(user));
        }
    }

    @ProbeTarget
    static final class Failing {
        @Test
        void fail(TestReporter reporter,
                  @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user) {
            reporter.publishEntry(buildEntries(user));
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
    static final class NegativeIncomeInvitations {
        @Test
        void observe(@User(incomeInvitations = -1) TestUser user) {
        }
    }

    @ProbeTarget
    static final class NegativeOutcomeInvitations {
        @Test
        void observe(@User(outcomeInvitations = -1) TestUser user) {
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

    private static Map<String, String> buildEntries(TestUser user) {
        TestUser friend = user.friends().getFirst();
        TestUser inviter = user.incomeInvitations().getFirst();
        TestUser addressee = user.outcomeInvitations().getFirst();

        Map<String, String> map = new HashMap<>();

        map.put("username", user.username());
        map.put("password", user.password());
        map.put("friend", friend.username());
        map.put("friendPassword", friend.password());
        map.put("inviter", inviter.username());
        map.put("inviterPassword", inviter.password());
        map.put("addressee", addressee.username());
        map.put("addresseePassword", addressee.password());

        map.put("friends", String.valueOf(user.friends().size()));
        map.put("incomeInvitations", String.valueOf(user.incomeInvitations().size()));
        map.put("outcomeInvitations", String.valueOf(user.outcomeInvitations().size()));

        map.put("account", String.valueOf(new AuthAccount(user.username(), user.password()).exists()));
        map.put("profile", String.valueOf(new ApiProfile(user.username()).exists()));

        map.put("friendship", String.valueOf(
                new Friendship(user.username(), friend.username(), FriendshipStatus.ACCEPTED).exists()));
        map.put("invitation", String.valueOf(
                new Friendship(inviter.username(), user.username(), FriendshipStatus.PENDING).exists()));
        map.put("request", String.valueOf(
                new Friendship(user.username(), addressee.username(), FriendshipStatus.PENDING).exists()));

        return map;
    }
}
