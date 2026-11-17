package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.jupiter.annotation.ProbeTest;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@ProbeTest
@DisplayName("ThemeExtension")
class ThemeExtensionTest {
    @Test
    @DisplayName("@Theme тёмной темы на методе включает тёмную тему")
    void darkThemeOnMethodEnablesDarkTheme() {
        assertEquals("true", entry(ThemeDarkOnMethodTarget.class, "dark"),
                "Page did not render dark theme for @Theme(DARK) on the method");
    }

    @Test
    @DisplayName("Аннотация метода сильнее аннотации класса")
    void methodAnnotationOverridesClass() {
        assertEquals("false", entry(ThemeMethodOverridesClassTarget.class, "dark"),
                "Method @Theme(LIGHT) did not override class @Theme(DARK)");
    }

    @Test
    @DisplayName("Без аннотации тема светлая")
    void withoutAnnotationThemeIsLight() {
        assertEquals("false", entry(ThemeDefaultLightTarget.class, "dark"),
                "Page rendered dark theme without @Theme");
    }

    @Test
    @DisplayName("Тема уже стоит на первой загруженной странице")
    void themeIsSetOnFirstLoadedPage() {
        assertEquals("true", entry(ThemeBeforeFirstPaintTarget.class, "darkOnFirstLoad"),
                "First loaded page was not dark before any test actions");
    }

    @Test
    @DisplayName("@Theme действует на контекст @Session")
    void themeAppliesToSessionContext() {
        assertEquals("true", entry(ThemeSessionTarget.class, "sessionDark"),
                "Session context did not render dark theme for @Theme(DARK)");
    }

    private String entry(Class<?> target, String key) {
        EngineExecutionResults results = run(target);
        assertEquals(1L, results.testEvents().succeeded().count(),
                "Probe target " + target.getSimpleName() + " did not succeed");
        Map<String, String> entries = new HashMap<>();
        results.allEvents().reportingEntryPublished().stream()
                .map(event -> event.getRequiredPayload(ReportEntry.class))
                .forEach(entry -> entries.putAll(entry.getKeyValuePairs()));
        String value = entries.get(key);
        if (value == null) {
            throw new IllegalStateException("Probe target " + target.getSimpleName() + " did not publish " + key);
        }
        return value;
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
}
