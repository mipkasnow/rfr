package io.student.rangiffler.jupiter.probe;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureExternalKey;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.FileSystemResultsWriter;
import io.qameta.allure.model.TestResult;
import io.student.rangiffler.jupiter.annotation.ProbeTarget;
import io.student.rangiffler.jupiter.annotation.UiTest;
import io.student.rangiffler.jupiter.condition.ProbeTargetCondition;
import io.student.rangiffler.visual.ScreenshotDiff;
import io.student.rangiffler.visual.ScreenshotMismatchError;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Tag("engine-probe")
@DisplayName("ScreenshotDiffExtension")
class ScreenshotDiffExtensionTest {
    @Test
    @DisplayName("Прикладывает по одному image diff на каждый ScreenshotMismatchError из assertAll")
    void attachesOneDiffPerMismatchInAssertAll(@TempDir Path dir) {
        List<JsonNode> diffs = diffs(run(MismatchesInAssertAll.class, dir.resolve("allure-results-5309")));
        assertAll("Image diffs of assertAll do not match its screenshot mismatches",
                () -> assertEquals(2, diffs.size(), "Mismatches of assertAll did not give exactly two image diffs"),
                () -> assertEquals(Set.of("Screenshot diff basket-31", "Screenshot diff avatar-8c"),
                        diffs.stream().map(diff -> diff.get("name").asString()).collect(Collectors.toSet()),
                        "Image diffs of assertAll are not named after its mismatched screenshots"));
    }

    @Test
    @DisplayName("Кладёт во вложение эталон, текущий скриншот и разницу как data URL PNG")
    void attachmentHoldsAllThreeImages(@TempDir Path dir) {
        Path results = run(SingleMismatch.class, dir.resolve("allure-results-7716"));
        JsonNode content = content(results, diffs(results).getFirst());
        Base64.Encoder encoder = Base64.getEncoder();
        assertAll("Image diff attachment does not hold the images of the mismatch",
                () -> assertEquals("data:image/png;base64," + encoder.encodeToString(new byte[]{17, -3, 88}),
                        content.get("expected").asString(), "Image diff does not hold the baseline screenshot"),
                () -> assertEquals("data:image/png;base64," + encoder.encodeToString(new byte[]{-41, 6}),
                        content.get("actual").asString(), "Image diff does not hold the actual screenshot"),
                () -> assertEquals("data:image/png;base64," + encoder.encodeToString(new byte[]{93, 0, -120, 54}),
                        content.get("diff").asString(), "Image diff does not hold the difference image"));
    }

    @Test
    @DisplayName("Находит ScreenshotMismatchError в причине другого исключения")
    void findsMismatchInCause(@TempDir Path dir) {
        List<JsonNode> diffs = diffs(run(MismatchAsCause.class, dir.resolve("allure-results-2648")));
        assertEquals(List.of("Screenshot diff toolbar-e1"),
                diffs.stream().map(diff -> diff.get("name").asString()).toList(),
                "Mismatch wrapped as a cause did not give its image diff");
    }

    @Test
    @DisplayName("Не прикладывает image diff к тесту, упавшему без ScreenshotMismatchError")
    void skipsTestWithoutMismatch(@TempDir Path dir) {
        assertEquals(0, diffs(run(PlainFailure.class, dir.resolve("allure-results-9052"))).size(),
                "Test without a screenshot mismatch got an image diff");
    }

    private Path run(Class<?> target, Path results) {
        AllureLifecycle origin = Allure.getLifecycle();
        AllureLifecycle probe = new AllureLifecycle(new FileSystemResultsWriter(results));
        ExecutorService thread = Executors.newSingleThreadExecutor();
        Allure.setLifecycle(probe);
        try {
            thread.submit(() -> execute(target, probe)).get(3, TimeUnit.MINUTES);
            return results;
        } catch (InterruptedException interruption) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while probe target " + target.getSimpleName() + " ran", interruption);
        } catch (ExecutionException | TimeoutException failure) {
            throw new IllegalStateException("Probe target " + target.getSimpleName() + " did not finish in its own thread", failure);
        } finally {
            Allure.setLifecycle(origin);
            thread.shutdownNow();
        }
    }

    private void execute(Class<?> target, AllureLifecycle lifecycle) {
        AllureExternalKey key = AllureExternalKey.random(ScreenshotDiffExtensionTest.class);
        lifecycle.scheduleTest(key, new TestResult().setUuid(UUID.randomUUID().toString()).setName(target.getSimpleName()));
        lifecycle.startTest(key);
        try {
            EngineExecutionResults results = EngineTestKit.engine("junit-jupiter")
                    .configurationParameter(ProbeTargetCondition.PARAMETER, "true")
                    .selectors(selectClass(target))
                    .execute();
            if (results.testEvents().started().count() != 1) {
                throw new IllegalStateException("Probe target " + target.getSimpleName() + " did not run its test: "
                        + results.allEvents().stream().map(Object::toString).collect(Collectors.joining(System.lineSeparator())));
            }
        } finally {
            lifecycle.stopTest(key);
            lifecycle.writeTest(key);
        }
    }

    private List<JsonNode> diffs(Path results) {
        try (Stream<Path> files = Files.list(results)) {
            return files.filter(file -> file.getFileName().toString().endsWith("-result.json"))
                    .map(file -> read(file))
                    .flatMap(this::attachments)
                    .filter(attachment -> attachment.get("type").asString().equals("application/vnd.allure.image.diff"))
                    .toList();
        } catch (IOException failure) {
            throw new UncheckedIOException("Allure results in " + results + " were not listed", failure);
        }
    }

    private Stream<JsonNode> attachments(JsonNode node) {
        return Stream.concat(node.get("attachments").valueStream(),
                node.get("steps").valueStream().flatMap(this::attachments));
    }

    private JsonNode content(Path results, JsonNode attachment) {
        return read(results.resolve(attachment.get("source").asString()));
    }

    private JsonNode read(Path file) {
        try {
            return JsonMapper.shared().readTree(Files.readString(file));
        } catch (IOException failure) {
            throw new UncheckedIOException("Allure file " + file + " was not read", failure);
        }
    }
}

@UiTest
@ProbeTarget
final class MismatchesInAssertAll {
    @Test
    void observe() {
        assertAll("Probe screenshots",
                () -> {
                    throw new ScreenshotMismatchError("basket differs",
                            new ScreenshotDiff("basket-31", new byte[]{4, 9}, new byte[]{5, 9}, new byte[]{1}));
                },
                () -> {
                    throw new ScreenshotMismatchError("avatar differs",
                            new ScreenshotDiff("avatar-8c", new byte[]{12}, new byte[]{13}, new byte[]{2, 2}));
                },
                () -> {
                    throw new AssertionError("title differs");
                });
    }
}

@UiTest
@ProbeTarget
final class SingleMismatch {
    @Test
    void observe() {
        throw new ScreenshotMismatchError("feed differs",
                new ScreenshotDiff("feed-a40", new byte[]{17, -3, 88}, new byte[]{-41, 6}, new byte[]{93, 0, -120, 54}));
    }
}

@UiTest
@ProbeTarget
final class MismatchAsCause {
    @Test
    void observe() {
        throw new IllegalStateException("toolbar check broke",
                new ScreenshotMismatchError("toolbar differs",
                        new ScreenshotDiff("toolbar-e1", new byte[]{71}, new byte[]{72}, new byte[]{73})));
    }
}

@UiTest
@ProbeTarget
final class PlainFailure {
    @Test
    void observe() {
        throw new AssertionError("menu label differs");
    }
}
