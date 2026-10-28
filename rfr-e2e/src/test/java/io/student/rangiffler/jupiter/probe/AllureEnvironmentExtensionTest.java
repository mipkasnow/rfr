package io.student.rangiffler.jupiter.probe;

import io.student.rangiffler.data.db.Databases;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.jupiter.extension.AllureEnvironmentExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("engine-probe")
@DisplayName("AllureEnvironmentExtension")
class AllureEnvironmentExtensionTest {

    @Test
    @DisplayName("После прогона во временном каталоге результатов есть environment.properties со всеми ключами")
    void writesEnvironmentProperties(@TempDir Path dir) throws IOException {
        Path results = dir.resolve("allure-results");
        String previousResults = System.getProperty(AllureEnvironmentExtension.RESULTS_DIRECTORY_PROPERTY);
        String previousCommit = System.getProperty("rfr.commit");
        System.setProperty(AllureEnvironmentExtension.RESULTS_DIRECTORY_PROPERTY, results.toString());
        System.setProperty("rfr.commit", "abc1234");
        try {
            EngineTestKit.engine("junit-jupiter")
                    .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                    .selectors(DiscoverySelectors.selectClass(Observed.class))
                    .execute();

            Path environment = results.resolve("environment.properties");
            assertTrue(Files.isRegularFile(environment), "environment.properties was not written");
            Properties properties = load(environment);
            assertEquals(
                    Set.of(
                            "frontUrl",
                            "authUrl",
                            "apiUrl",
                            "browser",
                            "headless",
                            "commit",
                            Databases.AUTH.schema(),
                            Databases.API.schema()),
                    properties.stringPropertyNames(),
                    "environment.properties keys do not match");
            assertEquals("abc1234", properties.getProperty("commit"));
            assertEquals("chromium", properties.getProperty("browser"));
            assertEquals("true", properties.getProperty("headless"));
        } finally {
            restore(AllureEnvironmentExtension.RESULTS_DIRECTORY_PROPERTY, previousResults);
            restore("rfr.commit", previousCommit);
        }
    }

    private static Properties load(Path environment) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(environment)) {
            properties.load(input);
        }
        return properties;
    }

    private static void restore(String key, String previous) {
        if (previous == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, previous);
        }
    }

    @ProbeTarget
    @ExtendWith(AllureEnvironmentExtension.class)
    static final class Observed {
        @Test
        void ready() {
        }
    }
}
