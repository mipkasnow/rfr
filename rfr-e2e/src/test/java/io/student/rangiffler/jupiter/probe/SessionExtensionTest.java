package io.student.rangiffler.jupiter.probe;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.ProbeTest;
import io.student.rangiffler.jupiter.annotation.Session;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.BrowserOptions;
import io.student.rangiffler.jupiter.extension.SessionExtension;
import io.student.rangiffler.session.GuestSession;
import io.student.rangiffler.session.UserSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@ProbeTest
@UsePlaywright(BrowserOptions.class)
@DisplayName("SessionExtension")
class SessionExtensionTest {
    @Test
    @DisplayName("Тест с гостевой сессией проходит и открывает гостевую страницу")
    void guestSessionTestSucceeds() {
        assertEquals(2L, run(SessionGuest.class).testEvents().succeeded().count(),
                "Test with a guest session did not succeed");
    }

    @Test
    @DisplayName("Гостевая сессия открывает второй контекст рядом с основным")
    void guestSessionOpensSecondContext() {
        assertEquals("2", runAndReturnEntries(SessionGuest.class).get("contextsDuring"),
                "Guest session did not open a second browser context");
    }

    @Test
    @DisplayName("После теста контекст гостевой сессии закрыт")
    void guestSessionContextIsClosedAfterTest() {
        assertEquals("1", runAndReturnEntries(SessionGuest.class).get("contextsAfter"),
                "Guest session context was not closed after the test");
    }

    @Test
    @DisplayName("Сессия пользователя приходит на главную страницу после входа")
    void userSessionTestSucceeds() {
        assertEquals(2L, run(SessionUser.class).testEvents().succeeded().count(),
                "Test with a user session did not reach the main page");
    }

    @Test
    @DisplayName("Во время теста пользователь сессии есть в auth")
    void userSessionAccountExistsDuringTest() {
        assertEquals("true", runAndReturnEntries(SessionUser.class).get("account"),
                "Account of the session user did not exist during the test");
    }

    @Test
    @DisplayName("После теста пользователя сессии нет в auth")
    void userSessionAccountIsRemovedAfterTest() {
        Map<String, String> entries = runAndReturnEntries(SessionUser.class);
        assertFalse(new AuthAccount(required(entries, "username"), required(entries, "password")).exists(),
                "Account of the session user was not removed after the test");
    }

    @Test
    @DisplayName("После теста контекст сессии пользователя закрыт")
    void userSessionContextIsClosedAfterTest() {
        assertEquals("1", runAndReturnEntries(SessionUser.class).get("contextsAfter"),
                "User session context was not closed after the test");
    }

    @Test
    @DisplayName("Сессия пользователя и @User TestUser получают разных пользователей")
    void sessionAndMainUserDiffer() {
        Map<String, String> entries = runAndReturnEntries(SessionBesideMainUser.class);
        assertNotEquals(entries.get("main"), entries.get("session"),
                "Session user and main user were the same user");
    }

    @Test
    @DisplayName("После упавшего теста пользователя сессии нет в auth")
    void userSessionAccountIsRemovedAfterFailedTest() {
        Map<String, String> entries = runAndReturnEntries(SessionFailing.class);
        assertFalse(new AuthAccount(required(entries, "username"), required(entries, "password")).exists(),
                "Account of the session user was not removed after the failed test");
    }

    @Test
    @DisplayName("После упавшего теста контекст сессии закрыт")
    void userSessionContextIsClosedAfterFailedTest() {
        assertEquals("1", runAndReturnEntries(SessionFailing.class).get("contextsAfter"),
                "User session context was not closed after the failed test");
    }

    @Test
    @DisplayName("UserSession без @User валит тест ExtensionConfigurationException")
    void userSessionWithoutUserFailsTest() {
        assertInstanceOf(ExtensionConfigurationException.class, failure(SessionUserWithoutUser.class),
                "UserSession without @User did not fail the test with ExtensionConfigurationException");
    }

    @Test
    @DisplayName("UserSession без @User не оставляет открытого контекста")
    void userSessionWithoutUserLeavesNoContext() {
        assertEquals("1", runAndReturnEntries(SessionUserWithoutUser.class).get("contextsAfter"),
                "UserSession without @User left a browser context open");
    }

    @Test
    @DisplayName("GuestSession с @User валит тест ExtensionConfigurationException")
    void guestSessionWithUserFailsTest() {
        assertInstanceOf(ExtensionConfigurationException.class, failure(SessionGuestWithUser.class),
                "GuestSession with @User did not fail the test with ExtensionConfigurationException");
    }

    @Test
    @DisplayName("GuestSession с @User не оставляет открытого контекста")
    void guestSessionWithUserLeavesNoContext() {
        assertEquals("1", runAndReturnEntries(SessionGuestWithUser.class).get("contextsAfter"),
                "GuestSession with @User left a browser context open");
    }

    @Test
    @DisplayName("После теста GuestSession с @User пользователя нет в auth")
    void guestSessionWithUserLeavesNoAccount() {
        Map<String, String> entries = runAndReturnEntries(SessionGuestWithUser.class);
        assertFalse(new AuthAccount(required(entries, "username"), required(entries, "password")).exists(),
                "Account of @User on GuestSession was not removed after the test");
    }

    @Test
    @DisplayName("SessionExtension над классом не открывает контекст тесту без @Session")
    void testWithoutSessionGetsNoContext() {
        assertEquals("1", runAndReturnEntries(SessionAbsent.class).get("contextsDuring"),
                "SessionExtension opened a browser context for a test without @Session");
    }

    private EngineExecutionResults run(Class<?> target) {
        ExecutorService thread = Executors.newSingleThreadExecutor();
        try {
            return thread.submit(() -> EngineTestKit.engine("junit-jupiter")
                    .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                    .selectors(selectClass(target))
                    .execute()).get(3, TimeUnit.MINUTES);
        } catch (InterruptedException interruption) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while probe target " + target.getSimpleName() + " ran", interruption);
        } catch (ExecutionException | TimeoutException failure) {
            throw new IllegalStateException("Probe target " + target.getSimpleName() + " did not finish in its own thread", failure);
        } finally {
            thread.shutdownNow();
        }
    }

    private String required(Map<String, String> entries, String key) {
        if (!entries.containsKey(key)) {
            throw new IllegalStateException("Probe target did not publish " + key);
        }
        return entries.get(key);
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

    @UiTest
    @ProbeTarget
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    static final class SessionGuest {
        @Test
        @Order(1)
        void observe(TestReporter reporter, Page page, @Session GuestSession guest) {
            reporter.publishEntry("contextsDuring", String.valueOf(page.context().browser().contexts().size()));
            guest.landingPage().clickLogin();
        }

        @Test
        @Order(2)
        void afterwards(TestReporter reporter, Page page) {
            reporter.publishEntry("contextsAfter", String.valueOf(page.context().browser().contexts().size()));
        }
    }

    @UiTest
    @ProbeTarget
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    static final class SessionUser {
        @Test
        @Order(1)
        void observe(TestReporter reporter, @Session @User UserSession bob) {
            reporter.publishEntry(Map.of(
                    "username", bob.user().username(),
                    "password", bob.user().password(),
                    "account", String.valueOf(new AuthAccount(bob.user().username(), bob.user().password()).exists())));
            bob.mainPage().checkThatMainElementsVisible();
        }

        @Test
        @Order(2)
        void afterwards(TestReporter reporter, Page page) {
            reporter.publishEntry("contextsAfter", String.valueOf(page.context().browser().contexts().size()));
        }
    }

    @UiTest
    @ProbeTarget
    static final class SessionBesideMainUser {
        @Test
        void observe(TestReporter reporter, @User TestUser alice, @Session @User UserSession bob) {
            reporter.publishEntry(Map.of("main", alice.username(), "session", bob.user().username()));
        }
    }

    @UiTest
    @ProbeTarget
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    static final class SessionFailing {
        @Test
        @Order(1)
        void fail(TestReporter reporter, @Session @User UserSession bob) {
            reporter.publishEntry(Map.of("username", bob.user().username(), "password", bob.user().password()));
            throw new IllegalStateException("Probe target fails on purpose");
        }

        @Test
        @Order(2)
        void afterwards(TestReporter reporter, Page page) {
            reporter.publishEntry("contextsAfter", String.valueOf(page.context().browser().contexts().size()));
        }
    }

    @UiTest
    @ProbeTarget
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    static final class SessionUserWithoutUser {
        @Test
        @Order(1)
        void observe(@Session UserSession bob) {
        }

        @Test
        @Order(2)
        void afterwards(TestReporter reporter, Page page) {
            reporter.publishEntry("contextsAfter", String.valueOf(page.context().browser().contexts().size()));
        }
    }

    @UiTest
    @ProbeTarget
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    static final class SessionGuestWithUser {
        @Test
        @Order(1)
        @ExtendWith(UserExtensionTest.UserTakenByIndex.class)
        void observe(@Session @User GuestSession guest) {
        }

        @Test
        @Order(2)
        void afterwards(TestReporter reporter, Page page) {
            reporter.publishEntry("contextsAfter", String.valueOf(page.context().browser().contexts().size()));
        }
    }

    @UiTest
    @ProbeTarget
    @ExtendWith(SessionExtension.class)
    static final class SessionAbsent {
        @Test
        void observe(TestReporter reporter, Page page) {
            reporter.publishEntry("contextsDuring", String.valueOf(page.context().browser().contexts().size()));
        }
    }
}
