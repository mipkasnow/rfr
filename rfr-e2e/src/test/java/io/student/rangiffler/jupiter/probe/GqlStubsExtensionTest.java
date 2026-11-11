package io.student.rangiffler.jupiter.probe;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.ProbeTest;
import io.student.rangiffler.jupiter.annotation.Session;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.BrowserOptions;
import io.student.rangiffler.network.GqlStubs;
import io.student.rangiffler.session.UserSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@ProbeTest
@UsePlaywright(BrowserOptions.class)
@DisplayName("GqlStubsExtension")
class GqlStubsExtensionTest {
    @Test
    @DisplayName("Два параметра GqlStubs валят тест ParameterResolutionException")
    void twoGqlStubsParametersFailWithHint() {
        Throwable failure = failure(TwoGqlStubsParameters.class);
        assertInstanceOf(ParameterResolutionException.class, failure,
                "Two GqlStubs parameters did not fail with ParameterResolutionException");
        assertTrue(failure.getMessage().contains("@Session(useStubs = true)"),
                "Failure message did not hint at @Session(useStubs = true): " + failure.getMessage());
    }

    @Test
    @DisplayName("Без useStubs у сессии stubs() пустой")
    void sessionWithoutUseStubsHasEmptyStubs() {
        assertEquals("false", runAndReturnEntries(SessionWithoutStubs.class).get("stubsPresent"),
                "Session without useStubs returned a non-empty stubs()");
    }

    @Test
    @DisplayName("С useStubs = true у сессии stubs() непустой")
    void sessionWithUseStubsHasStubs() {
        assertEquals("true", runAndReturnEntries(SessionWithStubs.class).get("stubsPresent"),
                "Session with useStubs = true returned an empty stubs()");
    }

    @Test
    @DisplayName("Стабы основного контекста и сессии снимаются в close без ошибок")
    void stubsOfMainAndSessionCloseWithoutErrors() {
        assertEquals(1L, run(StubsClosedOnOpenContexts.class).testEvents().succeeded().count(),
                "Registering stubs on main and session contexts failed during close()");
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
    static final class TwoGqlStubsParameters {
        @Test
        void observe(GqlStubs main, GqlStubs other) {
        }
    }

    @UiTest
    @ProbeTarget
    static final class SessionWithoutStubs {
        @Test
        void observe(TestReporter reporter, @Session @User UserSession bob) {
            reporter.publishEntry("stubsPresent", String.valueOf(bob.stubs().isPresent()));
        }
    }

    @UiTest
    @ProbeTarget
    static final class SessionWithStubs {
        @Test
        void observe(TestReporter reporter, @Session(useStubs = true) @User UserSession bob) {
            reporter.publishEntry("stubsPresent", String.valueOf(bob.stubs().isPresent()));
        }
    }

    @UiTest
    @ProbeTarget
    static final class StubsClosedOnOpenContexts {
        @Test
        void observe(Page page, GqlStubs main, @Session(useStubs = true) @User UserSession bob) {
            main.shouldReturnBody("GetPeople", "gql/get-people-stubbed.json");
            bob.stubs().orElseThrow().shouldReturnBody("GetPeople", "gql/get-people-stubbed.json");
            page.title();
            bob.mainPage().checkThatMainElementsVisible();
        }
    }
}
